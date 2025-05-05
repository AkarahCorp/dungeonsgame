package dev.akarah.dungeons;

import dev.akarah.dungeons.config.GlobalData;
import dev.akarah.dungeons.config.item.InventoryEvents;
import dev.akarah.dungeons.dungeon.DungeonEvents;
import dev.akarah.dungeons.dungeon.DungeonManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    static Main MAIN;

    DungeonManager manager = new DungeonManager();
    World dungeonWorld;
    GlobalData data;

    @Override
    public void onEnable() {
        // Plugin startup logic
        MAIN = this;

        this.data = Bootstrapper.INSTANCE.data;

        this.dungeonWorld = Bukkit.createWorld(
                WorldCreator.name("dungeon_world")
                        .copy(Objects.requireNonNull(Bukkit.getWorld("world")))
        );

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> Main.getInstance().dungeonManager().tickDungeons(), 5, 5);
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> Main.getInstance().data().statsHolder().loopPlayerStats(), 2, 2);
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> InventoryEvents.updateAllInventories(), 5, 5);

        Bukkit.getPluginManager().registerEvents(new DungeonEvents(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryEvents(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance() {
        return MAIN;
    }

    public DungeonManager dungeonManager() {
        return this.manager;
    }

    public World dungeonWorld() {
        return dungeonWorld;
    }

    public GlobalData data() {
        return data;
    }

    public NamespacedKey createKey(String path) {
        return new NamespacedKey("akarahnet", path);
    }
}
