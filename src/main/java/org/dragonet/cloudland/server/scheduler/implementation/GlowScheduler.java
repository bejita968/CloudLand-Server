package org.dragonet.cloudland.server.scheduler.implementation;

import org.dragonet.cloudland.server.CloudLandServer;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.dragonet.cloudland.server.scheduler.ScheduledTask;
import org.dragonet.cloudland.server.scheduler.BukkitWorker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A scheduler for managing server ticks, Bukkit tasks, and other synchronization.
 */
public final class GlowScheduler {

    /**
     * The number of milliseconds between pulses.
     */
    static final int PULSE_EVERY = 50;
    /**
     * The server this scheduler is managing for.
     */
    private final CloudLandServer server;
    /**
     * The scheduled executor service which backs this worlds.
     */
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(GlowThreadFactory.INSTANCE);
    /**
     * Executor to handle execution of async tasks
     */
    private final ExecutorService asyncTaskExecutor = Executors.newCachedThreadPool(GlowThreadFactory.INSTANCE);
    /**
     * A list of active tasks.
     */
    private final ConcurrentMap<Integer, GlowTask> tasks = new ConcurrentHashMap<>();
    /**
     * World tick scheduler
     */
    private final WorldScheduler worlds;
    /**
     * Tasks to be executed during the tick
     */
    private final Deque<Runnable> inTickTasks = new ConcurrentLinkedDeque<>();
    /**
     * Condition to wait on when processing in tick tasks
     */
    private final Object inTickTaskCondition;
    /**
     * Runnable to run at end of tick
     */
    private final Runnable tickEndRun;
    /**
     * The primary worlds thread in which pulse() is called.
     */
    private Thread primaryThread;

    /**
     * Creates a new task scheduler.
     *
     * @param server The server that will use this scheduler.
     * @param worlds The {@link WorldScheduler} this scheduler will use for ticking the server's worlds.
     */
    public GlowScheduler(CloudLandServer server, WorldScheduler worlds) {
        this.server = server;
        this.worlds = worlds;
        inTickTaskCondition = worlds.getAdvanceCondition();
        tickEndRun = this.worlds::doTickEnd;
        primaryThread = Thread.currentThread();
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                pulse();
            } catch (Exception ex) {
                // GlowServer.logger.log(Level.SEVERE, "Error while pulsing", ex);
            }
        }, 0, PULSE_EVERY, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the scheduler and all tasks.
     */
    public void stop() {
        cancelAllTasks();
        worlds.stop();
        executor.shutdownNow();
        asyncTaskExecutor.shutdown();

        synchronized (inTickTaskCondition) {
            inTickTasks.stream().filter(task -> task instanceof Future).forEach(task -> ((Future) task).cancel(false));
            inTickTasks.clear();
        }
    }

    /**
     * Schedules the specified task.
     *
     * @param task The task.
     */
    private GlowTask schedule(GlowTask task) {
        tasks.put(task.getTaskId(), task);
        return task;
    }

    /**
     * Checks if the current {@link Thread} is the server's primary thread.
     *
     * @return If the current {@link Thread} is the server's primary thread.
     */
    public boolean isPrimaryThread() {
        return Thread.currentThread() == primaryThread;
    }

    public void scheduleInTickExecution(Runnable run) {
        if (isPrimaryThread() || executor.isShutdown()) {
            run.run();
        } else {
            synchronized (inTickTaskCondition) {
                inTickTasks.addFirst(run);
                inTickTaskCondition.notifyAll();
            }
        }
    }

    /**
     * Adds new tasks and updates existing tasks, removing them if necessary.
     * <br>
     * todo: Add watchdog system to make sure ticks advance
     */
    private void pulse() {
        primaryThread = Thread.currentThread();

        long tickStart = System.currentTimeMillis();

        // Process player packets
        server.getNetwork().processMessages();

        // Run the relevant tasks.
        for (Iterator<GlowTask> it = tasks.values().iterator(); it.hasNext(); ) {
            GlowTask task = it.next();
            switch (task.shouldExecute()) {
                case RUN:
                    if (task.isSync()) {
                        task.run();
                    } else {
                        asyncTaskExecutor.submit(task);
                    }
                    break;
                case STOP:
                    it.remove();
            }
        }
        try {
            int currentTick = worlds.beginTick();
            try {
                asyncTaskExecutor.submit(tickEndRun);
            } catch (RejectedExecutionException ex) {
                worlds.stop();
                return;
            }

            Runnable tickTask;
            synchronized (inTickTaskCondition) {
                while (!worlds.isTickComplete(currentTick)) {
                    while ((tickTask = inTickTasks.poll()) != null) {
                        tickTask.run();
                    }

                    inTickTaskCondition.wait();
                }
            }

            long tickEnd = System.currentTimeMillis();
            // System.out.println("Tick finished in [" + (tickEnd - tickStart) + "] ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    public int scheduleSyncDelayedTask(String plugin, Runnable task, long delay) {
        return scheduleSyncRepeatingTask(plugin, task, delay, -1);
    }

    public int scheduleSyncDelayedTask(String plugin, Runnable task) {
        return scheduleSyncDelayedTask(plugin, task, 0);
    }

    public int scheduleSyncRepeatingTask(String plugin, Runnable task, long delay, long period) {
        return schedule(new GlowTask(plugin, task, true, delay, period)).getTaskId();
    }

    public int scheduleAsyncDelayedTask(String plugin, Runnable task, long delay) {
        return scheduleAsyncRepeatingTask(plugin, task, delay, -1);
    }

    public int scheduleAsyncDelayedTask(String plugin, Runnable task) {
        return scheduleAsyncRepeatingTask(plugin, task, 0, -1);
    }

    public int scheduleAsyncRepeatingTask(String plugin, Runnable task, long delay, long period) {
        return schedule(new GlowTask(plugin, task, false, delay, period)).getTaskId();
    }

    public <T> Future<T> callSyncMethod(String plugin, Callable<T> task) {
        FutureTask<T> future = new FutureTask<>(task);
        runTask(plugin, future);
        return future;
    }

    public <T> T syncIfNeeded(Callable<T> task) throws Exception {
        if (isPrimaryThread()) {
            return task.call();
        } else {
            return callSyncMethod(null, task).get();
        }
    }

    public ScheduledTask runTask(String plugin, Runnable task) throws IllegalArgumentException {
        return runTaskLater(plugin, task, 0);
    }

    public ScheduledTask runTaskAsynchronously(String plugin, Runnable task) throws IllegalArgumentException {
        return runTaskLaterAsynchronously(plugin, task, 0);
    }

    public ScheduledTask runTaskLater(String plugin, Runnable task, long delay) throws IllegalArgumentException {
        return runTaskTimer(plugin, task, delay, -1);
    }

    public ScheduledTask runTaskLaterAsynchronously(String plugin, Runnable task, long delay) throws IllegalArgumentException {
        return runTaskTimerAsynchronously(plugin, task, delay, -1);
    }

    public ScheduledTask runTaskTimer(String plugin, Runnable task, long delay, long period) throws IllegalArgumentException {
        return schedule(new GlowTask(plugin, task, true, delay, period));
    }

    public ScheduledTask runTaskTimerAsynchronously(String plugin, Runnable task, long delay, long period) throws IllegalArgumentException {
        return schedule(new GlowTask(plugin, task, false, delay, period));
    }

    public void cancelTask(int taskId) {
        tasks.remove(taskId);
    }

    public void cancelTasks(String plugin) {
        for (Iterator<GlowTask> it = tasks.values().iterator(); it.hasNext(); ) {
            if (it.next().getOwner() == plugin) {
                it.remove();
            }
        }
    }

    public void cancelAllTasks() {
        tasks.clear();
    }

    public boolean isCurrentlyRunning(int taskId) {
        GlowTask task = tasks.get(taskId);
        return task != null && task.getLastExecutionState() == TaskExecutionState.RUN;
    }

    public boolean isQueued(int taskId) {
        return tasks.containsKey(taskId);
    }

    /**
     * Returns active async tasks
     *
     * @return active async tasks
     */
    public List<BukkitWorker> getActiveWorkers() {
        return ImmutableList.copyOf(Collections2.filter(tasks.values(), glowTask -> glowTask != null && !glowTask.isSync() && glowTask.getLastExecutionState() == TaskExecutionState.RUN));
    }

    /**
     * Returns tasks that still have at least one run remaining
     *
     * @return the tasks to be run
     */
    public List<ScheduledTask> getPendingTasks() {
        return new ArrayList<>(tasks.values());
    }

    private static class GlowThreadFactory implements ThreadFactory {
        public static final GlowThreadFactory INSTANCE = new GlowThreadFactory();
        private final AtomicInteger threadCounter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "CloudLand-scheduler-" + threadCounter.getAndIncrement());
        }
    }
}
