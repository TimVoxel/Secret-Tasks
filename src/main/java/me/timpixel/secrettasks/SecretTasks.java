package me.timpixel.secrettasks;

import me.timpixel.fivelifes.FiveLifes;
import me.timpixel.secrettasks.commands.TaskCommand;
import me.timpixel.secrettasks.commands.TasksCommand;
import me.timpixel.secrettasks.view.BookGiver;
import me.timpixel.secrettasks.view.HealthManager;
import me.timpixel.secrettasks.view.TaskView;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class SecretTasks extends JavaPlugin {

    private static SecretTasks instance;
    private String tasksSaveFile;
    private TaskBase taskBase;

    @Override
    public void onEnable() {

        //singleton setup
        instance = this;

        //config
        FileConfiguration config = getConfig();
        config.addDefault("tasksSaveFilePath", getDataFolder().getPath());
        config.addDefault("maxHealth", 60.0);
        config.addDefault("taskCompletionHealthBonus", 20.0);
        config.addDefault("secondsBeforeTaskDistribution", 10);
        config.options().copyDefaults(true);
        saveConfig();

        tasksSaveFile = config.getString("tasksSaveFilePath");
        double maxHealth = config.getDouble("maxHealth");
        double taskCompletionBonus = config.getDouble("taskCompletionHealthBonus");
        int secondsBeforeTasks = config.getInt("secondsBeforeTaskDistribution");

        //create the task base
        this.taskBase = new TaskBase();

        //create the task distributor
        TaskDistributor taskDistributor = new TaskDistributor(taskBase, secondsBeforeTasks);

        //register task distributor as a listener
        FiveLifes.getSessionManager().listeners.add(taskDistributor);
        FiveLifes.getLifeBase().listeners.add(taskDistributor);

        //other mechanics and view
        BookGiver bookGiver = new BookGiver(taskDistributor);
        taskDistributor.listeners.add(bookGiver);

        HealthManager healthManager = new HealthManager(maxHealth, taskCompletionBonus);
        taskDistributor.listeners.add(healthManager);

        TaskView taskView = new TaskView();
        taskDistributor.listeners.add(taskView);

        //load in the tasks and the distribution
        taskBase.load();
        taskDistributor.load();

        //command setup
        registerCommand("tasks", new TasksCommand(taskBase, taskDistributor));
        registerCommand("task", new TaskCommand(taskDistributor));

        //minecraft event listener setup
        getServer().getPluginManager().registerEvents(bookGiver, this);
        getServer().getPluginManager().registerEvents(healthManager, this);

        for (World world : Bukkit.getWorlds())
            world.setGameRule(GameRule.NATURAL_REGENERATION, false);

        log(Level.INFO, "Secret-Tasks plugin loaded successfully");
    }

    public void registerCommand(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    public static String getTasksSaveFile() {
        return instance.tasksSaveFile;
    }
}