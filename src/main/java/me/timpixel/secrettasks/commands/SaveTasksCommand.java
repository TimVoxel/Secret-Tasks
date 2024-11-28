package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SaveTasksCommand implements SubCommand {

    private final TasksCommand root;

    public SaveTasksCommand(TasksCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        root.getTaskBase().save();
        root.getTaskDistributor().save();
        commandSender.sendMessage("Attempted to save tasks. Check logs for more info");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
