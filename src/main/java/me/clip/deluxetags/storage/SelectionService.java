package me.clip.deluxetags.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Serializes blocking storage work and publishes snapshots on the server thread.
 * Lifecycle/mutation entry points are called on the server thread; getters are thread safe.
 */
public final class SelectionService implements AutoCloseable {
    public enum Result { SUCCESS, UNAVAILABLE, LOADING, BUSY }

    private static final class Session {
        private final boolean applyInitially;
        private volatile Selection value;
        private Session(boolean applyInitially) { this.applyInitially = applyInitially; }
    }

    private final SelectionStore store;
    private final Executor worker, main;
    private final Consumer<UUID> changed;
    private final Consumer<String> log;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Object> pending = new ConcurrentHashMap<>();
    private final AtomicBoolean polling = new AtomicBoolean();
    private volatile boolean available, closed;
    private volatile long failureGeneration;
    private boolean initialized;
    private boolean reportedFailure;

    public SelectionService(SelectionStore store, Executor worker, Executor main,
                            Consumer<UUID> changed, Consumer<String> log) {
        this.store = store;
        this.worker = worker;
        this.main = main;
        this.changed = changed;
        this.log = log;
    }

    public boolean isAvailable() { return available && !closed; }
    public boolean isLoaded(UUID uuid) {
        Session session = sessions.get(uuid);
        return session != null && session.value != null;
    }

    public String getIdentifier(UUID uuid) {
        Session session = sessions.get(uuid);
        if (session != null && session.value != null) return session.value.getIdentifier();
        if (store instanceof YamlSelectionStore) return ((YamlSelectionStore) store).get(uuid).getIdentifier();
        return null;
    }

    public void join(UUID uuid, boolean applyInitially) {
        if (closed) return;
        Session session = new Session(applyInitially);
        sessions.put(uuid, session);
        if (!isAvailable()) {
            // Batch joins during startup/outages instead of queueing a connection timeout per player.
            poll();
            return;
        }
        Map<UUID, Session> snapshot = new HashMap<>();
        snapshot.put(uuid, session);
        execute(() -> refresh(snapshot, false));
    }

    public void quit(UUID uuid) {
        sessions.remove(uuid);
        // Keep any pending write until it completes, even across a rapid reconnect.
    }

    public void poll() {
        if (closed || !polling.compareAndSet(false, true)) return;
        execute(() -> refresh(new HashMap<>(sessions), true));
    }

    private void refresh(Map<UUID, Session> snapshot, boolean fullPoll) {
        if (closed) return;
        try {
            if (!initialized) {
                store.initialize();
                initialized = true;
            }
            Map<UUID, Selection> result = store.load(snapshot.keySet());
            long generation = failureGeneration;
            publish(() -> {
                for (Map.Entry<UUID, Session> entry : snapshot.entrySet()) {
                    apply(entry.getKey(), entry.getValue(), result.getOrDefault(entry.getKey(), Selection.ABSENT), false);
                }
                // Only a full poll reconciles all uncertain writes before changes resume.
                if (fullPoll) {
                    if (generation == failureGeneration) {
                        available = true;
                        if (reportedFailure) log.accept("Player selection storage recovered; selections refreshed.");
                        reportedFailure = false;
                    }
                    polling.set(false);
                }
            });
        } catch (Exception e) {
            fail(e);
            if (fullPoll) polling.set(false);
        }
    }

    private void apply(UUID uuid, Session session, Selection value, boolean mutation) {
        if (session == null || sessions.get(uuid) != session) return;
        Selection previous = session.value;
        if (previous != null && value.getRevision() <= previous.getRevision()) return;
        session.value = value;
        if (mutation || previous != null || session.applyInitially) changed.accept(uuid);
    }

    public void write(UUID uuid, String identifier, Consumer<Result> completion) {
        write(uuid, identifier, true, completion);
    }

    /** Compatibility writes may address an offline UUID, but still report completion asynchronously. */
    public void write(UUID uuid, String identifier, boolean requireLoaded, Consumer<Result> completion) {
        if (!isAvailable()) { completion.accept(Result.UNAVAILABLE); return; }
        Session session = sessions.get(uuid);
        if (requireLoaded && (session == null || session.value == null)) { completion.accept(Result.LOADING); return; }
        Object operation = new Object();
        if (pending.putIfAbsent(uuid, operation) != null) { completion.accept(Result.BUSY); return; }
        execute(() -> {
            // A preceding operation may have detected an outage after this request was accepted.
            if (!available) {
                publish(() -> complete(uuid, session, operation, Result.UNAVAILABLE, completion));
                return;
            }
            try {
                Selection value = store.write(uuid, identifier);
                publish(() -> {
                    apply(uuid, session, value, true);
                    complete(uuid, session, operation, Result.SUCCESS, completion);
                });
            } catch (Exception e) {
                fail(e);
                publish(() -> complete(uuid, session, operation, Result.UNAVAILABLE, completion));
            }
        });
    }

    private void complete(UUID uuid, Session session, Object operation, Result result, Consumer<Result> completion) {
        pending.remove(uuid, operation);
        if (sessions.get(uuid) == session) completion.accept(result);
    }

    private void fail(Exception error) {
        failureGeneration++;
        available = false;
        // JDBC exception text can contain connection details. Log only its type.
        publish(() -> {
            if (!reportedFailure) log.accept("Player selection storage unavailable (" + error.getClass().getSimpleName()
                    + "). Changes are paused; automatic retries will continue. Check database connectivity and configuration.");
            reportedFailure = true;
        });
    }

    private void execute(Runnable task) {
        if (closed) return;
        try { worker.execute(task); } catch (RejectedExecutionException ignored) { }
    }

    private void publish(Runnable task) {
        if (closed) return;
        try { main.execute(() -> { if (!closed) task.run(); }); }
        catch (RejectedExecutionException ignored) { }
    }

    /** YAML-only reload; callers recreate online sessions afterward to invalidate old callbacks. */
    public void reloadYaml() {
        if (!(store instanceof YamlSelectionStore)) return;
        sessions.clear();
        initialized = false;
        available = false;
        poll();
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        if (worker instanceof ExecutorService) {
            ExecutorService executor = (ExecutorService) worker;
            executor.execute(store::close);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.accept("Storage shutdown exceeded ten seconds; some pending changes may not have completed.");
                    executor.shutdownNow();
                    Thread cleanup = new Thread(store::close, "DeluxeTags-storage-close");
                    cleanup.setDaemon(true);
                    cleanup.start();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread cleanup = new Thread(store::close, "DeluxeTags-storage-close");
                cleanup.setDaemon(true);
                cleanup.start();
                Thread.currentThread().interrupt();
            }
        } else {
            store.close();
        }
        sessions.clear();
        pending.clear();
    }
}
