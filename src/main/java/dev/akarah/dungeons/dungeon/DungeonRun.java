package dev.akarah.dungeons.dungeon;

import dev.akarah.dungeons.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

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

        var map = getMapItem();
        for(var member : members) {
            var player = Bukkit.getServer().getPlayer(member);
            if(player == null) {
                continue;
            }
            player.teleportAsync(origin).thenRun(() -> {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.LEVITATION, 20, 1, true, false)
                );
                player.getInventory().setItem(EquipmentSlot.OFF_HAND, map);
                if(origin.getChunk().isForceLoaded()) {
                    this.place();
                    origin.getChunk().setForceLoaded(false);
                }
            });
        }
    }

    private @NotNull ItemStack getMapItem() {
        var map = ItemStack.of(Material.FILLED_MAP);
        map.editMeta(meta -> {
            if(meta instanceof MapMeta mapMeta) {
                var mapView = Bukkit.createMap(origin.getWorld());
                mapView.setCenterX((int) origin.x());
                mapView.setCenterZ((int) origin.z());
                mapView.setTrackingPosition(true);
                mapView.setUnlimitedTracking(true);
                mapView.setScale(MapView.Scale.CLOSE);
                mapMeta.setMapView(mapView);
                mapMeta.setScaling(true);
            }
        });
        return map;
    }

    public void end() {
        System.out.println("Ending " + this);
//        for(int x = -300; x<=300; x++) {
//            for(int y = -15; y<=15; y++) {
//                for(int z = -300; z<=300; z++) {
//                    origin.getWorld().setBlockData(
//                            origin.clone().add(x, y, z),
//                            Material.AIR.createBlockData()
//                    );
//                }
//            }
//        }
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
