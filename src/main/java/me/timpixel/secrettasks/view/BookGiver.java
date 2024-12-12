package me.timpixel.secrettasks.view;

import me.timpixel.secrettasks.TaskDistributor;
import me.timpixel.secrettasks.TaskListener;
import me.timpixel.secrettasks.TaskMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BookGiver implements TaskListener, Listener {

    private final Component taskBookAuthor = Component.text("Банк заданий").color(NamedTextColor.GOLD);
    private final NamespacedKey bookKey = NamespacedKey.minecraft("task_book");

    private final List<UUID> playersWhoDiedWithBook;

    private final TaskDistributor taskDistributor;

    public BookGiver(TaskDistributor taskDistributor) {
        this.taskDistributor = taskDistributor;
        this.playersWhoDiedWithBook = new ArrayList<>();
    }

    @Override
    public void onTasksDistributed(Map<UUID, TaskMeta> tasks) {
        for (Map.Entry<UUID, TaskMeta> entry : tasks.entrySet())
            onGotTask(entry.getKey(), entry.getValue());
    }

    @Override
    public void onTaskRerolled(UUID playerId, TaskMeta task) {
        onGotTask(playerId, task);
    }

    @Override
    public void onGotTask(UUID playerId, TaskMeta task) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null)
            return;

        givePlayerBook(player, task);
    }

    @EventHandler
    public void onPlayerLogsOn(PlayerJoinEvent event) {
        TaskMeta task = taskDistributor.get(event.getPlayer().getUniqueId());

        if (task == null)
            return;

        for (ItemStack item : event.getPlayer().getInventory())
        {
            if (item == null)
                continue;

            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            Boolean isBook = container.get(bookKey, PersistentDataType.BOOLEAN);

            if (isBook != null && isBook)
                return;
        }

        givePlayerBook(event.getPlayer(), task);
    }

    @EventHandler
    public void onPlayerDied(PlayerDeathEvent event) {
        boolean hadBook = removeBook(event.getDrops());
        if (hadBook)
            playersWhoDiedWithBook.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        for (UUID id : playersWhoDiedWithBook)
        {
            if (id.equals(event.getPlayer().getUniqueId())) {
                givePlayerBook(event.getPlayer(), taskDistributor.get(event.getPlayer().getUniqueId()));
                playersWhoDiedWithBook.remove(id);
                break;
            }
        }
    }

    private void givePlayerBook(@NotNull Player player, @NotNull TaskMeta task) {

        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) itemStack.getItemMeta();
        meta.author(taskBookAuthor);
        meta.setTitle("Задание " + player.getName());
        meta.setGeneration(BookMeta.Generation.ORIGINAL);

        if (!task.isBaked())
            task.bakeRandomizedParts(player.getUniqueId());

        meta.addPages(Component.text(task.getBakedText()));
        meta.getPersistentDataContainer().set(bookKey, PersistentDataType.BOOLEAN, true);

        itemStack.setItemMeta(meta);

        if (player.getInventory().firstEmpty() == -1)
        {
            Item item = player.getWorld().dropItem(player.getLocation(), itemStack);
            item.setOwner(player.getUniqueId());
        }
        else
            player.getInventory().addItem(itemStack);
    }

    @Override
    public void onFailedTask(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null)
            return;
        removeBook(player);
    }

    @Override
    public void onCompletedTask(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null)
            return;
        removeBook(player);
    }

    private boolean removeBook(Player player) {
        boolean hadBook = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item == null)
                continue;

            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            Boolean isBook = container.get(bookKey, PersistentDataType.BOOLEAN);

            if (isBook != null && isBook)
            {
                player.getInventory().setItem(i, null);
                hadBook = true;
            }
        }
        return hadBook;
    }

    private boolean removeBook(List<ItemStack> items) {
        boolean hadBook = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);

            if (item == null)
                continue;

            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            Boolean isBook = container.get(bookKey, PersistentDataType.BOOLEAN);

            if (isBook != null && isBook)
            {
                items.set(i, null);
                hadBook = true;
            }
        }
        return hadBook;
    }
}
