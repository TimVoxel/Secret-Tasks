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

public class TaskFailCommand implements SubCommand {

    private final TaskCommand root;

    public TaskFailCommand(TaskCommand root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return "fail";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(sender instanceof Player))
        {
            sender.sendMessage(Component.text("Эта команда предназначена для использования игроками").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        root.getTaskDistributor().registerFailedTask(player.getUniqueId());
        if (FiveLifes.getLifeBase().get(player.getUniqueId()) == 1)
            sender.sendMessage(Component.text("Вы провалили свое задание, но так как вы находитесь на последней жизни, вы сразу же получаете новое!").color(NamedTextColor.RED));
        else
            sender.sendMessage(Component.text("Задание помечено как проваленное. Удачи в следующий раз").color(NamedTextColor.RED));

        SecretTasks.log(Level.INFO, "Player " + player.getName() + " marked their task as failed");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}