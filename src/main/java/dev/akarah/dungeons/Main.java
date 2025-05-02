package dev.akarah.dungeons;

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

    @Override
    public void onEnable() {
        // Plugin startup logic
        MAIN = this;

        this.dungeonWorld = Bukkit.createWorld(
                WorldCreator.name("dungeon_world")
                        .copy(Objects.requireNonNull(Bukkit.getWorld("world")))
        );

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> Main.getInstance().dungeonManager().autoClearDungeons(), 1, 1);
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
}
