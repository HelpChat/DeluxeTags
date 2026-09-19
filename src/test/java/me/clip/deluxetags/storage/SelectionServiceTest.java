package me.clip.deluxetags.storage;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Test;

public class SelectionServiceTest {
    private static final UUID PLAYER = UUID.randomUUID();

    private static class Queue implements Executor {
        final Deque<Runnable> tasks = new ArrayDeque<>();
        public void execute(Runnable task) { tasks.add(task); }
        void drain() { while (!tasks.isEmpty()) tasks.removeFirst().run(); }
    }

    private static class Store implements SelectionStore {
        final Map<UUID, Selection> rows = new HashMap<>();
        boolean offline, ambiguousWrite, closed;
        int loads, writes, initializations;
        public void initialize() throws IOException {
            initializations++;
            if (offline) throw new IOException("secret connection details");
        }
        public Map<UUID, Selection> load(Collection<UUID> ids) throws IOException {
            loads++;
            if (offline) throw new IOException("secret connection details");
            Map<UUID, Selection> result = new HashMap<>();
            for (UUID id : ids) result.put(id, rows.getOrDefault(id, Selection.ABSENT));
            return result;
        }
        public Selection write(UUID id, String identifier) throws IOException {
            writes++;
            if (offline) throw new IOException();
            Selection value = new Selection(identifier, rows.getOrDefault(id, Selection.ABSENT).getRevision() + 1);
            rows.put(id, value);
            if (ambiguousWrite) throw new IOException("commit acknowledgement lost");
            return value;
        }
        public void close() { closed = true; }
    }

    private static class Fixture {
        final Store store = new Store();
        final Queue worker = new Queue(), main = new Queue();
        final List<UUID> changed = new ArrayList<>();
        final List<String> logs = new ArrayList<>();
        final SelectionService service = new SelectionService(store, worker, main, changed::add, logs::add);
        void drain() { worker.drain(); main.drain(); }
        void start() { service.poll(); service.join(PLAYER, true); drain(); changed.clear(); }
    }

    @Test public void acknowledgesOnlyPersistedChangesAndRejectsRapidClicks() {
        Fixture f = new Fixture(); f.start();
        List<SelectionService.Result> results = new ArrayList<>();
        f.service.write(PLAYER, "vip", results::add);
        assertNull(f.service.getIdentifier(PLAYER));
        assertTrue(results.isEmpty());
        f.service.write(PLAYER, "other", results::add);
        assertEquals(Collections.singletonList(SelectionService.Result.BUSY), results);
        f.worker.drain();
        assertNull(f.service.getIdentifier(PLAYER));
        f.main.drain();
        assertEquals("vip", f.service.getIdentifier(PLAYER));
        assertEquals(SelectionService.Result.SUCCESS, results.get(1));
        assertEquals(1, f.store.writes);
        assertEquals(Collections.singletonList(PLAYER), f.changed);
    }

    @Test public void propagatesRemoteSelectionsExplicitNoTagAndClearedPreference() {
        Fixture f = new Fixture(); f.start();
        for (String identifier : Arrays.asList("vip", Selection.NO_TAG, null, "unknown-on-this-server")) {
            long revision = f.store.rows.getOrDefault(PLAYER, Selection.ABSENT).getRevision() + 1;
            f.store.rows.put(PLAYER, new Selection(identifier, revision));
            f.service.poll(); f.drain();
            assertEquals(identifier, f.service.getIdentifier(PLAYER));
        }
        assertEquals(4, f.changed.size());
        assertEquals(0, f.store.writes); // Applying a remote value never writes it back.
    }

    @Test public void ignoresOlderSnapshotsEvenWhenCallbacksAreDelayed() {
        Fixture f = new Fixture(); f.start();
        f.store.rows.put(PLAYER, new Selection("old", 1));
        f.service.poll(); f.worker.drain();
        f.service.write(PLAYER, "new", ignored -> { }); f.worker.drain();
        f.main.tasks.removeLast().run(); // Commit callback arrives before an older poll.
        f.main.drain();
        assertEquals("new", f.service.getIdentifier(PLAYER));
        assertEquals(1, f.changed.size());
    }

    @Test public void outageKeepsCacheRejectsWritesAndRecoversWithoutReplay() {
        Fixture f = new Fixture();
        f.store.rows.put(PLAYER, new Selection("vip", 1)); f.start();
        f.store.offline = true;
        f.service.poll(); f.drain();
        assertEquals("vip", f.service.getIdentifier(PLAYER));
        List<SelectionService.Result> results = new ArrayList<>();
        f.service.write(PLAYER, "lost", results::add);
        assertEquals(Collections.singletonList(SelectionService.Result.UNAVAILABLE), results);
        assertEquals(0, f.store.writes);
        UUID newcomer = UUID.randomUUID();
        f.service.join(newcomer, true); f.drain();
        assertFalse(f.service.isLoaded(newcomer));
        f.store.offline = false;
        f.store.rows.put(PLAYER, new Selection("remote", 2));
        f.service.poll(); f.drain();
        assertTrue(f.service.isAvailable());
        assertTrue(f.service.isLoaded(newcomer));
        assertEquals("remote", f.service.getIdentifier(PLAYER));
        assertFalse(f.logs.toString().contains("secret connection details"));
        assertEquals(0, f.store.writes);
    }

    @Test public void reconcilesUncertainCommitWithoutReportingSuccessOrReplaying() {
        Fixture f = new Fixture(); f.start();
        f.store.ambiguousWrite = true;
        List<SelectionService.Result> results = new ArrayList<>();
        f.service.write(PLAYER, "committed", results::add); f.drain();
        assertEquals(Collections.singletonList(SelectionService.Result.UNAVAILABLE), results);
        assertNull(f.service.getIdentifier(PLAYER));
        f.service.poll(); f.drain();
        assertEquals("committed", f.service.getIdentifier(PLAYER));
        assertEquals(1, f.store.writes);
    }

    @Test public void startupFailureRetriesAndPollingDoesNotOverlap() {
        Fixture f = new Fixture(); f.store.offline = true;
        f.service.join(PLAYER, true); f.drain();
        assertFalse(f.service.isLoaded(PLAYER));
        f.store.offline = false;
        f.service.poll(); f.service.poll();
        assertEquals(1, f.worker.tasks.size());
        f.drain();
        assertTrue(f.service.isAvailable());
        assertTrue(f.service.isLoaded(PLAYER));
    }

    @Test public void batchesJoinStormDuringStartupOrOutage() {
        Fixture f = new Fixture();
        for (int i = 0; i < 100; i++) f.service.join(UUID.randomUUID(), true);
        assertEquals(1, f.worker.tasks.size());
        f.drain();
        assertEquals(1, f.store.loads);
        assertEquals(100, f.changed.size());
    }

    @Test public void joinCanLoadWithoutApplyingUntilARealChange() {
        Fixture f = new Fixture();
        f.store.rows.put(PLAYER, new Selection("vip", 1));
        f.service.poll(); f.service.join(PLAYER, false); f.drain();
        assertTrue(f.service.isLoaded(PLAYER));
        assertTrue(f.changed.isEmpty());
        f.service.poll(); f.drain();
        assertTrue(f.changed.isEmpty());
        f.store.rows.put(PLAYER, new Selection("other", 2));
        f.service.poll(); f.drain();
        assertEquals(Collections.singletonList(PLAYER), f.changed);
    }

    @Test public void oldSessionCannotApplyOrReceiveWriteCallbacksAfterReconnect() {
        Fixture f = new Fixture(); f.start();
        List<SelectionService.Result> results = new ArrayList<>();
        f.service.write(PLAYER, "vip", results::add);
        f.worker.drain();
        f.service.quit(PLAYER);
        f.service.join(PLAYER, true);
        f.main.drain();
        assertFalse(f.service.isLoaded(PLAYER));
        assertTrue(results.isEmpty());
        f.drain();
        assertEquals("vip", f.service.getIdentifier(PLAYER));
        assertEquals(1, f.changed.size());
    }

    @Test public void unloadedSessionRejectsSelectionWithoutWriting() {
        Fixture f = new Fixture(); f.start();
        UUID newcomer = UUID.randomUUID();
        f.service.join(newcomer, true);
        List<SelectionService.Result> results = new ArrayList<>();
        f.service.write(newcomer, "vip", results::add);
        assertEquals(Collections.singletonList(SelectionService.Result.LOADING), results);
        assertEquals(0, f.store.writes);
        f.drain();
        assertTrue(f.service.isLoaded(newcomer));
    }

    @Test public void oldJoinReadCannotInitializeNewSession() {
        Fixture f = new Fixture();
        f.store.rows.put(PLAYER, new Selection("old", 1));
        f.service.join(PLAYER, true); f.worker.drain();
        f.service.quit(PLAYER); f.service.join(PLAYER, true);
        f.store.rows.put(PLAYER, new Selection("new", 2));
        f.main.drain();
        assertFalse(f.service.isLoaded(PLAYER));
        assertTrue(f.changed.isEmpty());
        f.service.poll(); f.drain();
        assertEquals("new", f.service.getIdentifier(PLAYER));
    }

    @Test public void oldPollCannotRestoreAvailabilityAfterNewerFailure() {
        Fixture f = new Fixture(); f.start();
        f.service.poll(); f.worker.drain();
        f.store.offline = true;
        f.service.write(PLAYER, "vip", ignored -> { }); f.worker.drain();
        f.main.drain();
        assertFalse(f.service.isAvailable());
    }

    @Test public void shutdownSuppressesAlreadyQueuedCallbacks() {
        Fixture f = new Fixture(); f.start();
        f.service.write(PLAYER, "vip", ignored -> fail("Late callback"));
        f.worker.drain();
        f.service.close(); f.main.drain();
        assertTrue(f.changed.isEmpty());
        assertTrue(f.store.closed);
    }

    @Test public void shutdownDrainsAcceptedWrites() throws Exception {
        Store store = new Store();
        java.util.concurrent.ExecutorService worker = Executors.newSingleThreadExecutor();
        SelectionService service = new SelectionService(store, worker, Runnable::run, ignored -> { }, ignored -> { });
        service.poll(); service.join(PLAYER, true);
        worker.submit(() -> { }).get();
        service.write(PLAYER, "saved-before-close", ignored -> { });
        service.close();
        assertEquals("saved-before-close", store.rows.get(PLAYER).getIdentifier());
        assertTrue(store.closed);
    }
}
