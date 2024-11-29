package me.timpixel.secrettasks;

import me.timpixel.fivelifes.FiveLifes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TaskMeta {

    private static final Random random = new Random();

    private boolean isBaked;
    private final String text;
    private final UUID id;
    private final List<UUID> unsuitableFor;
    private final boolean isRed;
    private final boolean isHard;

    private String bakedText;

    public TaskMeta(String text, UUID id, List<UUID> unsuitableFor, boolean isRed, boolean isHard) {
        this.text = text;
        this.id = id;
        this.unsuitableFor = unsuitableFor;
        this.isRed = isRed;
        this.isHard = isHard;
        this.isBaked = false;
    }

    public void bakeRandomizedParts(UUID playerId) {

        if (isBaked)
            return;

        bakedText = text;
        List<UUID> usedUUIDs = new ArrayList<>();
        usedUUIDs.add(playerId);

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        while (bakedText.contains("<>")) {

            Player player;
            do {
                int playerIndex = random.nextInt(0, onlinePlayers.size());
                player = onlinePlayers.get(playerIndex);
            } while (usedUUIDs.contains(player.getUniqueId()) || FiveLifes.getLifeBase().get(player) == 0);

            usedUUIDs.add(player.getUniqueId());
            bakedText = bakedText.replaceFirst("<>", player.getName());
        }

        isBaked = true;
    }

    @SuppressWarnings("unchecked")
    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        obj.put("text", text);
        obj.put("id", id.toString());
        obj.put("is_red", isRed);
        obj.put("is_hard", isHard);

        JSONArray unsuitableForJson = new JSONArray();
        for (UUID playerId : unsuitableFor)
            unsuitableForJson.add(playerId.toString());

        obj.put("unsuitable_for", unsuitableForJson);
        return obj;
    }

    public static TaskMeta deserialize(JSONObject object) {
        String text = (String) object.get("text");
        UUID id = UUID.fromString((String) object.get("id"));
        boolean isRed = (boolean) object.get("is_red");
        boolean isHard = (boolean) object.get("is_hard");
        JSONArray unsuitableForJSON = (JSONArray) object.get("unsuitable_for");

        List<UUID> unsuitableFor = new ArrayList<>();

        for (Object entry : unsuitableForJSON) {
            String playerId = (String) entry;
            unsuitableFor.add(UUID.fromString(playerId));
        }
        return new TaskMeta(text, id, unsuitableFor, isRed, isHard);
    }

    public boolean isBaked() {
        return isBaked;
    }

    public String getText() {
        return text;
    }

    public String getBakedText() {
        return bakedText;
    }

    public UUID getId() {
        return id;
    }

    public List<UUID> getUnsuitableFor() {
        return unsuitableFor;
    }

    public boolean isRed() {
        return isRed;
    }

    public boolean isHard() {
        return isHard;
    }
}