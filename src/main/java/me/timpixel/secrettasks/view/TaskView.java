package me.timpixel.secrettasks.view;

import me.timpixel.fivelifes.FiveLifes;
import me.timpixel.secrettasks.TaskListener;
import me.timpixel.secrettasks.TaskMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class TaskView implements TaskListener {

    private final Title tasksDistributedMessage = Title.title(Component.text( "Вы получили задание!").color(NamedTextColor.YELLOW), Component.text("Удачи!"));
    private final Title taskCompletedMessage  =Title.title(Component.text("Задание выполнено!").color(NamedTextColor.GREEN), Component.text("+10 сердец"));
    private final Title taskFailedMessage = Title.title(Component.text("Задание провалено").color(NamedTextColor.RED), Component.text("Удачи в следующий раз"));
    private final Title taskRerolledMessage = Title.title(Component.text("Получено сложное задание"), Component.text("Теперь точно удачи..."));

    @Override
    public void onTasksDistributed(Map<UUID, TaskMeta> tasks) {
        for (Map.Entry<UUID, TaskMeta> entry : tasks.entrySet())
            onGotTask(entry.getKey(), entry.getValue());
    }

    @Override
    public void onCompletedTask(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);

        if (player != null)
        {
            player.showTitle(taskCompletedMessage);
        }
    }

    @Override
    public void onFailedTask(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null)
            player.showTitle(taskFailedMessage);
    }

    @Override
    public void onTaskRerolled(UUID playerId, TaskMeta task) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null)
            player.showTitle(taskRerolledMessage);
    }

    @Override
    public void onGotTask(UUID playerId, TaskMeta task) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null)
            player.showTitle(tasksDistributedMessage);
    }
}
