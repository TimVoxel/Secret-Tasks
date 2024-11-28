package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FormatTasksCommand implements SubCommand {

    private final TasksCommand root;

    public FormatTasksCommand(TasksCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "format";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        root.getTaskBase().format();
        commandSender.sendMessage("Attempted to format tasks. Check logs for more info");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}