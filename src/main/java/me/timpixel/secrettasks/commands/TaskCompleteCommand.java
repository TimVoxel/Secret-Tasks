package me.timpixel.secrettasks.commands;

import me.timpixel.fivelifes.FiveLifes;
import me.timpixel.fivelifes.commands.SubCommand;
import me.timpixel.secrettasks.SecretTasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TaskCompleteCommand implements SubCommand {

    private final TaskCommand root;

    public TaskCompleteCommand(TaskCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "complete";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(sender instanceof Player))
        {
            sender.sendMessage(Component.text("Эта команда предназначена для использования игроками").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        root.getTaskDistributor().registerCompletedTask(player.getUniqueId());
        if (FiveLifes.getLifeBase().get(player.getUniqueId()) != 1)
            player.sendMessage(Component.text("Задание помечено как выполненное. Вы получите свою награду в конце сессии").color(NamedTextColor.GREEN));
        else
            player.sendMessage(Component.text("Вы выполнили свое задание и сразу же получаете новое, так как находитесь на последней жизни").color(NamedTextColor.GREEN));

        SecretTasks.log(Level.INFO, "Player " + player.getName() + " marked their task as completed");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}