package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.SubCommand;
import me.timpixel.secrettasks.SecretTasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ResetTasksCommand implements SubCommand {

    private final List<String> tabCompletion;

    private final TasksCommand root;

    public ResetTasksCommand(TasksCommand root) {
        this.root = root;
        this.tabCompletion = new ArrayList<>();
        tabCompletion.add("distribution");
        tabCompletion.add("used");
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0)
        {
            commandSender.sendMessage(Component.text("Specify what to reset").color(NamedTextColor.RED));
            return true;
        }

        if (args[0].equalsIgnoreCase("distribution"))
        {
            boolean result = root.getTaskDistributor().resetDistribution();
            if (result)
            {
                SecretTasks.log(Level.INFO, "Deleted task distribution");
                commandSender.sendMessage(Component.text("Deleted taskDistribution.json (and cleared it)"));
            }
            else
            {
                SecretTasks.log(Level.SEVERE, "Couldn't delete distribution");
                commandSender.sendMessage(Component.text("Couldn't delete distribution").color(NamedTextColor.RED));
            }
        }
        else if (args[0].equalsIgnoreCase("used")) {
            boolean result = root.getTaskBase().resetUsedTasks();
            if (result)
            {
                SecretTasks.log(Level.INFO, "Deleted used task list");
                commandSender.sendMessage(Component.text("Deleted used task list (and cleared it)"));
            }
            else
            {
                SecretTasks.log(Level.SEVERE, "Couldn't delete task list");
                commandSender.sendMessage(Component.text("Couldn't delete task list").color(NamedTextColor.RED));
            }

        }
        else {
            commandSender.sendMessage(Component.text("Invalid argument: " + args[0]));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return tabCompletion;
    }
}
