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

public class TaskRerollCommand implements SubCommand {

    private final TaskCommand root;

    public TaskRerollCommand(TaskCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "reroll";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(sender instanceof Player))
        {
            sender.sendMessage(Component.text("Эта команда предназначена для использования игроками").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        TaskAssignmentResult result = root.getTaskDistributor().rerollTask(player.getUniqueId());

        switch (result) {
            case FailedHasHardTask:
                sender.sendMessage(Component.text("У вас уже есть сложное задание").color(NamedTextColor.RED));
                break;
            case FailedHasNoTask:
                sender.sendMessage(Component.text("Нельзя получить сложное задание, не имея обычного. Используйте /task get, чтобы получить его").color(NamedTextColor.RED));
                break;
            case FailedIsDead:
                sender.sendMessage(Component.text("Вы не можете получить задание, так как у вас не осталось жизней").color(NamedTextColor.RED));
                break;
            case FailedIsRed:
                sender.sendMessage(Component.text("Красные жизни не могут получать сложные задания").color(NamedTextColor.RED));
                break;
            case FailedNoHardTasksLoaded:
                sender.sendMessage(Component.text("Нет загруженных сложных заданий").color(NamedTextColor.RED));
                break;
            case FailedNoAppropriateTasksFound:
                sender.sendMessage(Component.text("Не найдено подходящего задания. Возможно, все сложные задания уже были использованы").color(NamedTextColor.RED));
                break;
            case Successful:
                sender.sendMessage(Component.text("Вы получили новое сложное задание").color(NamedTextColor.YELLOW));
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}