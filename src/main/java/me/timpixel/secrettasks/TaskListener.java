package me.timpixel.secrettasks;

import java.util.Map;
import java.util.UUID;

public interface TaskListener {

    void onTasksDistributed(Map<UUID, TaskMeta> tasks);
    void onCompletedTask(UUID playerId);
    void onFailedTask(UUID player);
    void onTaskRerolled(UUID player, TaskMeta task);
    void onGotTask(UUID player, TaskMeta task);
}
