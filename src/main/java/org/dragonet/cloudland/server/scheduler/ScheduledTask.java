package org.dragonet.cloudland.server.scheduler;

/**
 * Represents a task being executed by the scheduler
 */
public interface ScheduledTask {

    /**
     * Returns the taskId for the task.
     *
     * @return Task id number
     */
    public int getTaskId();

    /**
     * Returns the Plugin that owns this task.
     *
     * @return The Plugin that owns the task
     */
    public String getOwner();

    /**
     * Returns true if the Task is a sync task.
     *
     * @return true if the task is run by main thread
     */
    public boolean isSync();

    /**
     * Will attempt to cancel this task.
     */
    public void cancel();
}
