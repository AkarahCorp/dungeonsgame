package dev.akarah.dungeons.dungeon;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import dev.akarah.dungeons.Main;

public record DungeonRun(
        Location origin,
        List<UUID> members,
        AtomicInteger failures) {
    public int sumGearScores() {
        var sumScore = 0;

        for (var member : members) {
            sumScore += Main.getInstance().data().statsHolder().getGearScore(member);
        }

        return sumScore;
    }

    public void start() {
        origin.getChunk().load(true);
        origin.getChunk().setForceLoaded(true);
        origin.getWorld().loadChunk(origin.getChunk());

        var map = getMapItem();
        for (var member : members) {
            var player = Bukkit.getServer().getPlayer(member);
            if (player == null) {
                continue;
            }
            player.teleportAsync(origin).thenRun(() -> {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.LEVITATION, 20, 1, true, false));
                player.getInventory().setItem(EquipmentSlot.OFF_HAND, map);
                if (origin.getChunk().isForceLoaded()) {
                    this.place();
                    origin.getChunk().setForceLoaded(false);
                }
            });

        }
    }

    private @NotNull ItemStack getMapItem() {
        var map = ItemStack.of(Material.FILLED_MAP);
        map.editMeta(meta -> {
            if (meta instanceof MapMeta mapMeta) {
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
            if (origin.getChunk().isLoaded()) {
                Bukkit.getGlobalRegionScheduler().runDelayed(Main.getInstance(), task2 -> {
                    System.out.println("task: " + task2);
                    Bukkit.getServer().dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "execute in minecraft:dungeon_world run place jigsaw akarahnet:catacombs/start akarahnet:catacombs/spawn_room 20 "
                                    + (int) origin.x() + " " + (int) origin.y() + " " + (int) origin.z());
                }, 1);
            } else {
                this.place();
            }

        }, 1);
    }

    public void tick() {
        for (var member : this.members) {
            var p = Bukkit.getPlayer(member);
            if (p == null) {
                continue;
            }

            var center = p.getLocation();

            try {
                if (center.distance(origin) > 500) {
                    continue;
                }
            } catch (IllegalArgumentException exception) {
                continue;
            }

            var entities = center.getNearbyEntities(20, 20, 20);
            for (var entity : entities) {
                if (entity instanceof ArmorStand armorStand) {
                    var pos = armorStand.getLocation();
                    armorStand.remove();

                    var sumScore = this.sumGearScores();
                    var componentName = armorStand.customName();
                    if(componentName == null) {
                        componentName = Component.empty();
                    }
                    var plainTextName = PlainTextComponentSerializer.plainText().serialize(componentName);

                    var stringChoices = Main.getInstance().data().mobs()
                        .getAllOf(mob ->
                            mob.spawnRules().mobGenre().equals(plainTextName)
                                && sumScore >= mob.spawnRules().minScore()
                                && sumScore <= mob.spawnRules().maxScore()
                        );

                    try {
                        var mob = stringChoices.get(new Random().nextInt(0, stringChoices.size()));
                        mob.spawn(pos);
                    } catch (IllegalArgumentException ignored) {
                        // this can be ignored, this can only happen if a mob doesn't exist in the entry for the
                        // specified requirements
                    }
                }
            }
        }
    }
}
