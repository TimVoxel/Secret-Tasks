package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PrintTasksCommand implements SubCommand {

    private final TasksCommand root;

    public PrintTasksCommand(TasksCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        root.getTaskBase().print();
        commandSender.sendMessage("Printed all tasks to logs");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}