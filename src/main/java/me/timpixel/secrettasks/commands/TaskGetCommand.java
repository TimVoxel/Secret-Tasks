package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.commands.SubCommand;
import me.timpixel.secrettasks.resulttypes.TaskAssignmentResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TaskGetCommand implements SubCommand {

    private final TaskCommand root;

    public TaskGetCommand(TaskCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(sender instanceof Player))
        {
            sender.sendMessage(Component.text("Эта команда предназначена для использования игроками").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        TaskAssignmentResult result = root.getTaskDistributor().assignNewRandomTask(player.getUniqueId());

        switch (result) {
            case FailedHasTask:
                sender.sendMessage(Component.text("У вас уже есть задание. Используйте /task reroll, чтобы получить сложное задание").color(NamedTextColor.RED));
                break;
            case FailedIsDead:
                sender.sendMessage(Component.text("Вы не можете получить задание, так как у вас не осталось жизней").color(NamedTextColor.RED));
                break;
            case FailedNoAppropriateTasksFound:
                sender.sendMessage(Component.text("Не найдено подходящего задания. Возможно, все задания уже были использованы").color(NamedTextColor.RED));
                break;
            case FailedNoRedTasksLoaded:
                sender.sendMessage(Component.text("Нет загруженных красных заданий").color(NamedTextColor.RED));
                break;
            case Successful:
                sender.sendMessage(Component.text("Вы получили новое задание").color(NamedTextColor.YELLOW));
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}