package dev.akarah.dungeons.config.item;

import dev.akarah.dungeons.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerStatsHolder {
    HashMap<UUID, StatsObject> playerStats = new HashMap<>();

    public StatsObject statsFor(UUID uuid) {
        return playerStats.get(uuid);
    }

    public StatsObject statsFor(Player p) {
        return playerStats.get(p.getUniqueId());
    }

    public void setStats(Player p, StatsObject stats) {
        playerStats.put(p.getUniqueId(), stats);
    }

    public void setStats(UUID uuid, StatsObject stats) {
        playerStats.put(uuid, stats);
    }

    public void loopPlayerStats() {
        for(var p : Bukkit.getOnlinePlayers()) {
            var stats = StatsObject.baseStats();
            var items = new ItemStack[]{
                    p.getInventory().getItem(EquipmentSlot.HEAD),
                    p.getInventory().getItem(EquipmentSlot.CHEST),
                    p.getInventory().getItem(EquipmentSlot.LEGS),
                    p.getInventory().getItem(EquipmentSlot.FEET),
                    p.getInventory().getItem(EquipmentSlot.HAND),
                    p.getInventory().getItem(EquipmentSlot.OFF_HAND)
            };
            for(var item : items) {
                var itemId = CustomItem.getItemId(item);
                var customItemFromInv = Main.getInstance().data().items().get(itemId);
                if(customItemFromInv == null) {
                    continue;
                }
                stats = stats.add(customItemFromInv.stats());
            }

            try {
                Objects.requireNonNull(p.getAttribute(Attribute.ARMOR)).setBaseValue(stats.get(Stats.ARMOR));
                Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(stats.get(Stats.MAX_HEALTH));
                Objects.requireNonNull(p.getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(500.0);
                Objects.requireNonNull(p.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(stats.get(Stats.ATTACK_DAMAGE));
            } catch (NullPointerException exception) {
                throw new RuntimeException(exception);
            }

            this.setStats(p, stats);
        }
    }
}
