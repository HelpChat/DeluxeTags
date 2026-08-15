package me.clip.deluxetags.utils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import me.clip.deluxetags.DeluxeTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

/**
 * Scheduler bridge for Spigot, Paper and Folia without raising the minimum API version.
 */
public final class Scheduler {

  private static final boolean FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");

  private Scheduler() {
  }

  public static Cancellable runAsync(DeluxeTags plugin, Runnable task) {
    if (!FOLIA) {
      return fromBukkit(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    Object scheduler = invoke(Bukkit.class, null, "getAsyncScheduler");
    Object scheduled = invoke(scheduler.getClass(), scheduler, "runNow",
        plugin, (Consumer<Object>) ignored -> task.run());
    return fromFolia(scheduled);
  }

  public static Cancellable runAsyncTimer(DeluxeTags plugin, Runnable task,
      long initialDelay, long period, TimeUnit unit) {
    if (!FOLIA) {
      long initialTicks = Math.max(1L, unit.toMillis(initialDelay) / 50L);
      long periodTicks = Math.max(1L, unit.toMillis(period) / 50L);
      return fromBukkit(Bukkit.getScheduler().runTaskTimerAsynchronously(
          plugin, task, initialTicks, periodTicks));
    }

    Object scheduler = invoke(Bukkit.class, null, "getAsyncScheduler");
    Object scheduled = invoke(scheduler.getClass(), scheduler, "runAtFixedRate",
        plugin, (Consumer<Object>) ignored -> task.run(), initialDelay, period, unit);
    return fromFolia(scheduled);
  }

  public static void runGlobal(DeluxeTags plugin, Runnable task) {
    if (!FOLIA) {
      Bukkit.getScheduler().runTask(plugin, task);
      return;
    }

    Object scheduler = invoke(Bukkit.class, null, "getGlobalRegionScheduler");
    invoke(scheduler.getClass(), scheduler, "run",
        plugin, (Consumer<Object>) ignored -> task.run());
  }

  public static void runAtEntity(DeluxeTags plugin, Entity entity, Runnable task) {
    if (!FOLIA) {
      Bukkit.getScheduler().runTask(plugin, task);
      return;
    }

    Object scheduler = invoke(entity.getClass(), entity, "getScheduler");
    invoke(scheduler.getClass(), scheduler, "run",
        plugin, (Consumer<Object>) ignored -> task.run(), null);
  }

  public static boolean isOwnedByCurrentThread(Entity entity) {
    if (!FOLIA) {
      return Bukkit.isPrimaryThread();
    }
    Object result = invoke(Bukkit.class, null, "isOwnedByCurrentRegion", entity);
    return Boolean.TRUE.equals(result);
  }

  /**
   * Executes Bukkit entity API work in the entity's region and returns its result.
   * This is intended for asynchronous event callbacks which must finish before returning.
   */
  public static <T> T callAtEntity(DeluxeTags plugin, Entity entity, Supplier<T> supplier,
      T fallback) {
    if (isOwnedByCurrentThread(entity)) {
      return supplier.get();
    }

    CompletableFuture<T> future = new CompletableFuture<>();
    runAtEntity(plugin, entity, () -> {
      try {
        future.complete(supplier.get());
      } catch (Throwable throwable) {
        future.completeExceptionally(throwable);
      }
    });

    try {
      return future.get(5L, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException | TimeoutException ex) {
      plugin.getLogger().warning("Entity-scheduled operation failed: " + ex.getMessage());
    }
    return fallback;
  }

  public static boolean isFolia() {
    return FOLIA;
  }

  private static Cancellable fromBukkit(BukkitTask task) {
    return task::cancel;
  }

  private static Cancellable fromFolia(Object task) {
    return () -> invoke(task.getClass(), task, "cancel");
  }

  private static Object invoke(Class<?> type, Object target, String name, Object... args) {
    for (Method method : type.getMethods()) {
      if (!method.getName().equals(name) || method.getParameterTypes().length != args.length) {
        continue;
      }
      try {
        return method.invoke(target, args);
      } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
        // An overload with the same arity may not match; try the next one.
      }
    }
    throw new IllegalStateException("Unable to invoke scheduler method " + type.getName() + '#' + name);
  }

  private static boolean classExists(String name) {
    try {
      Class.forName(name);
      return true;
    } catch (ClassNotFoundException ignored) {
      return false;
    }
  }

  public interface Cancellable {
    void cancel();
  }
}
