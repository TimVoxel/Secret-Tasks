package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.FiveLifes;
import me.timpixel.fivelifes.commands.SubCommand;
import me.timpixel.secrettasks.SecretTasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TaskUnassignCommand implements SubCommand {

    private final TaskCommand root;

    public TaskUnassignCommand(TaskCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "unassign";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player))
        {
            sender.sendMessage(Component.text("Эта команда предназначена для использования игроками").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("life.admin"))
        {
            sender.sendMessage(Component.text("У вас нет прав на использование этой подкоманды").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(Component.text("Укажите игрока, которому следует убрать задание").color(NamedTextColor.RED));
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null)
        {
            sender.sendMessage(Component.text("Игрок с именем \"" + playerName + "\" не найден").color(NamedTextColor.RED));
            return true;
        }

        root.getTaskDistributor().unassignTask(target.getUniqueId());
        sender.sendMessage(Component.text("Задание игрока " + target.getName() + " убрано"));
        SecretTasks.log(Level.INFO, "Player " + player.getName() + "'s task was unassigned");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
