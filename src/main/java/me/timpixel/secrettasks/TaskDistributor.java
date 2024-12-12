package me.timpixel.secrettasks;

import me.timpixel.fivelifes.FiveLifes;
import me.timpixel.fivelifes.LifeBaseListener;
import me.timpixel.fivelifes.SessionListener;
import me.timpixel.secrettasks.resulttypes.TaskAssignmentResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class TaskDistributor implements SessionListener, LifeBaseListener {

    private final Random random = new Random();
    private final Component taskAnnouncementComponent = Component.text("Задания будут распределены через 5 минут").color(NamedTextColor.YELLOW);

    public final List<TaskListener> listeners;
    private final TaskBase taskBase;
    private final int secondsBeforeDistribution;
    private final Map<UUID, TaskMeta> taskDistribution;
    private final Map<UUID, Boolean> taskCompletionMap;

    public TaskDistributor(TaskBase taskBase, int secondsBeforeDistribution) {
        this.taskBase = taskBase;
        this.taskDistribution = new HashMap<>();
        this.taskCompletionMap = new HashMap<>();
        this.secondsBeforeDistribution = secondsBeforeDistribution;
        this.listeners = new ArrayList<>();
    }

    @Override
    public void onSessionStarted() {
        Bukkit.broadcast(taskAnnouncementComponent);
    }

    public TaskMeta get(UUID playerId) {
        return taskDistribution.get(playerId);
    }

    public TaskAssignmentResult assignNewRandomTask(UUID playerId) {

        if (taskDistribution.containsKey(playerId))
            return TaskAssignmentResult.FailedHasTask;

        int playerLifeCount = FiveLifes.getLifeBase().get(playerId);

        if (playerLifeCount == 0)
            return TaskAssignmentResult.FailedIsDead;

        taskCompletionMap.put(playerId, false);
        TaskAssignmentResult result = assignRandomTask(playerId, false);

        if (result == TaskAssignmentResult.Successful)
        {
            announcePlayerAssignedTask(playerId, get(playerId));
            taskBase.saveUsedTasks();
            save();
        }
        return result;
    }

    private TaskAssignmentResult assignRandomTask(UUID playerId, boolean filterHard) {

        int playerLifeCount = FiveLifes.getLifeBase().get(playerId);
        boolean filterRed = playerLifeCount == 1;

        if (filterRed && taskBase.getRedTasks().isEmpty())
            return TaskAssignmentResult.FailedNoRedTasksLoaded;

        if (filterHard && taskBase.getHardTasks().isEmpty())
            return TaskAssignmentResult.FailedNoHardTasksLoaded;

        List<TaskMeta> queryList = new ArrayList<>(
                        filterHard ? taskBase.getHardTasks() :
                        playerLifeCount == 1 ? taskBase.getRedTasks() :
                        taskBase.getNormalGreenTasks());

        if (!filterRed) {
            for (int i = queryList.size() - 1; i >= 0; i--)
            {
                TaskMeta task = queryList.get(i);

                if (taskDistribution.containsValue(task))
                    queryList.remove(i);
                else if (taskBase.isUsed(task))
                    queryList.remove(i);
                else if (task.getUnsuitableFor().contains(playerId))
                    queryList.remove(i);
            }
        }

        if (queryList.isEmpty())
            return TaskAssignmentResult.FailedNoAppropriateTasksFound;

        int taskIndex = random.nextInt(0, queryList.size());
        TaskMeta task = queryList.get(taskIndex);

        task.bakeRandomizedParts(playerId);
        taskBase.registerTaskAsUsed(task);
        taskDistribution.put(playerId, task);
        SecretTasks.log(Level.INFO, "Player " + Bukkit.getPlayer(playerId) + " has received task " + task.getId().toString());
        return TaskAssignmentResult.Successful;
    }


    public void unassignTask(UUID playerId) {
        taskDistribution.remove(playerId);
        taskCompletionMap.remove(playerId);
        save();
    }

    @SuppressWarnings("unchecked")
    public void save()  {

        new Thread(() -> {
            JSONObject content = new JSONObject();

            JSONObject distributionSerialized = new JSONObject();

            for (Map.Entry<UUID, TaskMeta> id : taskDistribution.entrySet())
                distributionSerialized.put(id.getKey().toString(), id.getValue().getId().toString());

            content.put("distribution", distributionSerialized);

            JSONObject completionSerialized = new JSONObject();

            for (Map.Entry<UUID, Boolean> entry : taskCompletionMap.entrySet())
                completionSerialized.put(entry.getKey().toString(), entry.getValue());

            content.put("completion", completionSerialized);

            try {
                FileWriter file = new FileWriter(new File(SecretTasks.getTasksSaveFile(), "distribution.json"));
                file.write(content.toJSONString());
                file.close();

            } catch (IOException e) {
                SecretTasks.log(Level.SEVERE, e.getMessage());
            }
        }).start();
    }

    public boolean resetDistribution() {
        File distribution = new File(SecretTasks.getTasksSaveFile(), "distribution.json");
        boolean result = false;

        if (distribution.exists())
            try {
                result = distribution.delete();
            }
            catch (SecurityException exception) {
                SecretTasks.log(Level.SEVERE, exception.getMessage());
            }
        
        if (result)
            taskDistribution.clear();
        
        return result;
    }

    @SuppressWarnings("unchecked")
    public void load() {

        taskDistribution.clear();
        taskCompletionMap.clear();

        File file = new File(SecretTasks.getTasksSaveFile(), "distribution.json");

        try {
            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(file);

            try {
                JSONObject root = (JSONObject) parser.parse(reader);
                JSONObject distribution = (JSONObject) root.get("distribution");
                JSONObject completion = (JSONObject) root.get("completion");

                for (Object entry : distribution.entrySet()) {
                    JSONObject.Entry<String, String> convertedEntry = (JSONObject.Entry<String, String>) entry;
                    UUID playerId = UUID.fromString(convertedEntry.getKey());
                    UUID taskId = UUID.fromString(convertedEntry.getValue());
                    TaskMeta task = null;

                    for (TaskMeta potentialTask : taskBase.getTasks())
                        if (potentialTask.getId().equals(taskId)) {
                            task = potentialTask;
                            break;
                        }

                    if (task == null)
                    {
                        SecretTasks.log(Level.SEVERE, "Could not find task assigned to player " + convertedEntry.getKey() + " with id " + convertedEntry.getValue());
                        continue;
                    }

                    taskDistribution.put(playerId, task);
                }

                for (Object entry : completion.entrySet()) {
                    JSONObject.Entry<String, Boolean> convertedEntry = (JSONObject.Entry<String, Boolean>) entry;
                    UUID playerId = UUID.fromString(convertedEntry.getKey());
                    Boolean value = convertedEntry.getValue();
                    taskCompletionMap.put(playerId, value);
                }
            }
            catch (ParseException e) {
                SecretTasks.log(Level.SEVERE, "ParseException: " + e.getMessage());
            }
            catch (IOException e) {
                SecretTasks.log(Level.SEVERE, "IOException: " + e.getMessage());
            }
            catch (ClassCastException e) {
                SecretTasks.log(Level.SEVERE, "CastException: " + e.getMessage());
            }

            reader.close();
        }
        catch (FileNotFoundException e) {
            SecretTasks.log(Level.INFO, "Save file \"distribution.json\" not found, distribution and completion not loaded");
        }
        catch (IOException e) {
            SecretTasks.log(Level.SEVERE, e.getMessage());
        }
    }

    public TaskAssignmentResult rerollTask(UUID playerId) {

        int playerLifeCount = FiveLifes.getLifeBase().get(playerId);

        if (playerLifeCount == 1)
            return TaskAssignmentResult.FailedIsRed;

        if (playerLifeCount == 0)
            return TaskAssignmentResult.FailedIsDead;

        if (!taskDistribution.containsKey(playerId))
            return TaskAssignmentResult.FailedHasNoTask;

        if (taskDistribution.get(playerId).isHard())
            return TaskAssignmentResult.FailedHasHardTask;

        taskDistribution.remove(playerId);
        taskCompletionMap.put(playerId, false);
        TaskAssignmentResult result = assignRandomTask(playerId, true);

        if (result == TaskAssignmentResult.Successful)
        {
            announcePlayerRerolledTask(playerId, get(playerId));
            taskBase.saveUsedTasks();
            save();
        }
        return result;
    }

    public void registerCompletedTask(UUID playerId) {

        int lifeCount = FiveLifes.getLifeBase().get(playerId);

        if (lifeCount == 0)
            return;

        if (lifeCount == 1)
        {
            announcePlayerCompletedTask(playerId);
            taskDistribution.remove(playerId);
            assignNewRandomTask(playerId);
        }
        else
            taskCompletionMap.put(playerId, true);
    }

    public void registerFailedTask(UUID playerId) {

        int lifeCount = FiveLifes.getLifeBase().get(playerId);

        if (lifeCount == 0)
            return;

        if (lifeCount == 1) {
            announcePlayerFailedTask(playerId);
            taskDistribution.remove(playerId);
            assignNewRandomTask(playerId);
        }

        else
            taskCompletionMap.put(playerId, false);
    }

    private void distributeTasks() {
        taskDistribution.clear();
        taskCompletionMap.clear();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player player : players) {
            if (FiveLifes.getLifeBase().get(player) == 0)
                continue;

            assignRandomTask(player.getUniqueId(), false);
            taskCompletionMap.put(player.getUniqueId(), false);
        }
        announceTasksDistributed();
        taskBase.saveUsedTasks();
        save();
    }

    @Override
    public void onSessionEnded() {

        for (Map.Entry<UUID, Boolean> entry : taskCompletionMap.entrySet()) {
            if (entry.getValue())
                announcePlayerCompletedTask(entry.getKey());
            else
                announcePlayerFailedTask(entry.getKey());
        }
    }

    @Override
    public void onSessionTick(int i) {
        if (i == secondsBeforeDistribution * 20)
            distributeTasks();
    }

    @Override
    public void onSessionTimeRanOut() {}

    private void announcePlayerCompletedTask(UUID player) {
        for (TaskListener listener : listeners)
            listener.onCompletedTask(player);
    }

    private void announcePlayerFailedTask(UUID player) {
        for (TaskListener listener : listeners)
            listener.onFailedTask(player);
    }

    private void announcePlayerRerolledTask(UUID player, TaskMeta task) {
        for (TaskListener listener : listeners)
            listener.onTaskRerolled(player, task);
    }

    private void announceTasksDistributed() {
        for (TaskListener listener : listeners)
            listener.onTasksDistributed(taskDistribution);
    }

    private void announcePlayerAssignedTask(UUID player, TaskMeta task) {
        for (TaskListener listener : listeners)
            listener.onGotTask(player, task);
    }

    @Override
    public void onLifeBaseUpdated(UUID uuid, int currentLife, int previousLife) {
        if (currentLife == 1 && currentLife != previousLife)
            assignNewRandomTask(uuid);
    }

    @Override
    public void onLifeBaseLoaded() {}
}
