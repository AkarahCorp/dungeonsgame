package dev.akarah.dungeons.dungeon;

import dev.akarah.dungeons.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public record DungeonRun(
        Location origin,
        List<UUID> members,
        AtomicInteger failures
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

    public void tick() {
        for(var member : this.members) {
            var p = Bukkit.getPlayer(member);
            if(p == null) {
                continue;
            }

            var center = p.getLocation();

            try {
                if(center.distance(origin) > 500) {
                    continue;
                }
            } catch (IllegalArgumentException exception) {
                continue;
            }

            var entities = center.getNearbyEntities(20, 20, 20);
            for(var entity : entities) {
                if(entity instanceof ArmorStand armorStand) {
                    var pos = armorStand.getLocation();
                    armorStand.remove();

                    center.getWorld().spawnEntity(
                            pos,
                            EntityType.ZOMBIE
                    );
                }
            }
        }
    }
}
