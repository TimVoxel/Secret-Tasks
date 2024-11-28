package me.timpixel.secrettasks.view;

import me.timpixel.secrettasks.TaskListener;
import me.timpixel.secrettasks.TaskMeta;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;
import java.util.UUID;

public class HealthManager implements Listener, TaskListener {

    private final double maxHealth;
    private final double taskCompletionBonus;

    public HealthManager(double maxHealth, double taskCompletionBonus) {
        this.maxHealth = maxHealth;
        this.taskCompletionBonus = taskCompletionBonus;
    }

    @EventHandler
    public void onPlayerLogged(PlayerJoinEvent event) {

        if (!event.getPlayer().hasPlayedBefore()){
            setMaxHealth(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerTakesDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        if (player.getHealth() - event.getFinalDamage() <= 0)
            return;

        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null)
            maxHealthAttribute.setBaseValue(player.getHealth() - event.getFinalDamage());
    }


    private void setMaxHealth(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null)
        {
            maxHealthAttribute.setBaseValue(maxHealth);
            player.setHealth(maxHealthAttribute.getBaseValue());
        }
    }

    private void addMaxHealth(Player player, double value) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null)
        {
            double newValue = Math.max(1f, Math.min(maxHealthAttribute.getBaseValue() + value, maxHealth));
            maxHealthAttribute.setBaseValue(newValue);
            player.setHealth(maxHealthAttribute.getBaseValue());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        setMaxHealth(event.getPlayer());
    }

    @Override
    public void onCompletedTask(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null)
            addMaxHealth(player, taskCompletionBonus);
    }

    @Override
    public void onFailedTask(UUID playerId) {}

    @Override
    public void onTasksDistributed(Map<UUID, TaskMeta> tasks) {}

    @Override
    public void onGotTask(UUID player, TaskMeta task) {}

    @Override
    public void onTaskRerolled(UUID player, TaskMeta task) {}
}
