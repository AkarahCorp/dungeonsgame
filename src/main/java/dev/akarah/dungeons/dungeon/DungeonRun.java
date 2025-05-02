package dev.akarah.dungeons.dungeon;

import dev.akarah.dungeons.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public record DungeonRun(
        Location origin,
        List<UUID> members
) {
    public void start() {
        origin.getChunk().load(true);
        origin.getChunk().setForceLoaded(true);
        origin.getWorld().loadChunk(origin.getChunk());

        for(var member : members) {
            var player = Bukkit.getServer().getPlayer(member);
            if(player == null) {
                continue;
            }
            player.teleportAsync(origin).thenRun(() -> {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.LEVITATION, 20, 1, true, false)
                );
                if(origin.getChunk().isForceLoaded()) {
                    this.place();
                    origin.getChunk().setForceLoaded(false);
                }
            });
        }
    }

    public void end() {
        System.out.println("Ending " + this);
        for(int x = -300; x<=300; x++) {
            for(int y = -15; y<=15; y++) {
                for(int z = -300; z<=300; z++) {
                    origin.getWorld().setBlockData(
                            origin.clone().add(x, y, z),
                            Material.AIR.createBlockData()
                    );
                }
            }
        }
    }

    public void place() {
        Bukkit.getGlobalRegionScheduler().runDelayed(Main.getInstance(), task -> {
            if(origin.getChunk().isLoaded()) {
                Bukkit.getGlobalRegionScheduler().runDelayed(Main.getInstance(), task2 -> {
                    System.out.println("task: " + task2);
                    Bukkit.getServer().dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "execute in minecraft:dungeon_world run place jigsaw akarahnet:catacombs/start akarahnet:catacombs/spawn_room 20 " + (int) origin.x() + " " + (int) origin.y() + " " + (int) origin.z()
                    );
                }, 1);
            } else {
                this.place();
            }

        }, 1);
    }
}
