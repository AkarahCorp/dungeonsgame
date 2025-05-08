package dev.akarah.dungeons.config.item;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import dev.akarah.dungeons.Main;

public class PlayerStatsHolder {
    HashMap<UUID, StatsObject> playerStats = new HashMap<>();
    HashMap<UUID, Integer> hitCooldown = new HashMap<>();
    HashMap<UUID, Integer> gearScore = new HashMap<>();

    public StatsObject statsFor(UUID uuid) {
        return playerStats.get(uuid);
    }

    public StatsObject statsFor(Player p) {
        return playerStats.get(p.getUniqueId());
    }

    public void setStats(Player p, StatsObject stats) {
        playerStats.put(p.getUniqueId(), stats);
    }

    public int getGearScore(UUID uuid) {
        return gearScore.getOrDefault(uuid, 0);
    }

    public void setHitCooldown(Player p, int cd) {
        hitCooldown.put(p.getUniqueId(), cd);
    }

    public int getHitCooldown(Player p) {
        return hitCooldown.getOrDefault(p.getUniqueId(), 1);
    }

    public void loopPlayerStats() {
        for (var p : Bukkit.getOnlinePlayers()) {
            this.hitCooldown.put(
                    p.getUniqueId(),
                    this.hitCooldown.getOrDefault(p.getUniqueId(), 0) - 1);

            var stats = StatsObject.baseStats();
            var totalGearScore = 0;
            var items = new ItemStack[] {
                    p.getInventory().getItem(EquipmentSlot.HEAD),
                    p.getInventory().getItem(EquipmentSlot.CHEST),
                    p.getInventory().getItem(EquipmentSlot.LEGS),
                    p.getInventory().getItem(EquipmentSlot.FEET),
                    p.getInventory().getItem(EquipmentSlot.HAND),
                    p.getInventory().getItem(EquipmentSlot.OFF_HAND)
            };
            for (var item : items) {
                var itemId = CustomItem.getItemId(item);
                var customItemFromInv = Main.getInstance().data().items().get(itemId);
                if (customItemFromInv == null) {
                    continue;
                }
                stats = stats.add(customItemFromInv.stats());
                totalGearScore += customItemFromInv.gearScore();
            }

            try {
                Objects.requireNonNull(p.getAttribute(Attribute.ARMOR)).setBaseValue(stats.get(Stats.ARMOR));
                Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(stats.get(Stats.MAX_HEALTH));
                Objects.requireNonNull(p.getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(500.0);
                Objects.requireNonNull(p.getAttribute(Attribute.ATTACK_DAMAGE))
                        .setBaseValue(stats.get(Stats.ATTACK_DAMAGE));
                Objects.requireNonNull(p.getAttribute(Attribute.MOVEMENT_SPEED))
                        .setBaseValue(0.1 + (stats.get(Stats.WALK_SPEED) / 20));
            } catch (NullPointerException exception) {
                throw new RuntimeException(exception);
            }

            this.setStats(p, stats);
            this.gearScore.put(p.getUniqueId(), totalGearScore);
        }
    }
}
