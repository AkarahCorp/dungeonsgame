package dev.akarah.dungeons.dungeon;

import dev.akarah.dungeons.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class DungeonEvents implements Listener {
    @EventHandler
    public void breakCoal(BlockBreakEvent event) {
        if(event.getBlock().getType().equals(Material.COAL_BLOCK)) {
            recursivelyBreakCoal(event.getBlock().getLocation());
        }
    }

    public void recursivelyFindCoal(Location location, ArrayList<Location> list) {
        var vectors = new Vector[]{
                new Vector(0, 1, 0),
                new Vector(0, -1, 0),
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1)
        };

        for(var vector : vectors) {
            var adjusted = location.clone().add(vector);
            if(adjusted.getBlock().getType().equals(Material.COAL_BLOCK)
            && !(list.contains(adjusted))) {
                list.add(adjusted);
                recursivelyFindCoal(adjusted, list);
            }
        }
    }

    public void recursivelyBreakCoal(Location location) {
        var coalBlocks = new ArrayList<Location>();
        recursivelyFindCoal(location, coalBlocks);

        for(var subLocation : coalBlocks) {
            subLocation.getBlock().setBlockData(Material.AIR.createBlockData());
            var falling = subLocation.getWorld().spawn(subLocation.clone().toCenterLocation(), FallingBlock.class);
            falling.setBlockState(Material.COAL_BLOCK.createBlockData().createBlockState());
            falling.setDropItem(false);
            falling.setMaxDamage(0);
            falling.setHurtEntities(false);
            falling.setCancelDrop(true);
        }
    }

    @EventHandler
    public void openChest(PlayerInteractEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType().equals(Material.CHEST)) {
            event.setCancelled(true);
            var blockState = event.getClickedBlock().getState();
            if(blockState instanceof Chest chest) {
                if(!chest.isOpen()) {
                    chest.open();

                    var choices = new String[]{
                        "food/bread"
                    };
                    var id = choices[new Random().nextInt(0, choices.length)];

                    event.getPlayer().getInventory().addItem(
                            Objects.requireNonNull(
                                    Main.getInstance().data().items().get(
                                            Objects.requireNonNull(NamespacedKey.fromString(id))
                                    )
                            ).toItemStack()
                    );
                }
            }
        }
    }

    @EventHandler
    public void shootProjectile(EntityShootBowEvent event) {
        var damage = Objects.requireNonNull(event.getEntity().getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
        var uuidStr = event.getEntity().getUniqueId().toString();
        event.getProjectile().getPersistentDataContainer()
                .set(Main.getInstance().createKey("arrow/damage"), PersistentDataType.DOUBLE, damage);
        event.getProjectile().getPersistentDataContainer()
                .set(Main.getInstance().createKey("arrow/shooter"), PersistentDataType.STRING, uuidStr);
    }

    @EventHandler
    public void landProjectile(ProjectileHitEvent event) {
        if(event.getEntity().getPersistentDataContainer().has(Main.getInstance().createKey("arrow/damage"))) {
            event.setCancelled(true);

            double damage = Objects.requireNonNull(
                    event.getEntity()
                            .getPersistentDataContainer()
                            .get(Main.getInstance().createKey("arrow/damage"), PersistentDataType.DOUBLE)
            );
            var attackerUuid = UUID.fromString(
                    Objects.requireNonNull(
                            event.getEntity()
                                    .getPersistentDataContainer()
                                    .get(Main.getInstance().createKey("arrow/shooter"), PersistentDataType.STRING)
                    )
            );

            var attackerEntity = event.getEntity().getWorld().getEntity(attackerUuid);
            var hitEntity = event.getHitEntity();

            if(attackerEntity == null && hitEntity != null) {
                if(hitEntity instanceof LivingEntity le) {
                    le.damage(damage);
                }
            }
            if(attackerEntity != null && hitEntity != null) {
                if(hitEntity instanceof LivingEntity le) {
                    le.damage(damage, attackerEntity);
                }
            }
        }

        event.getEntity().remove();
    }

    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        if(event.getEntity().getPersistentDataContainer()
                .has(Main.getInstance().createKey("entity/id"))) {
            var id = event.getEntity().getPersistentDataContainer()
                    .get(Main.getInstance().createKey("entity/id"), PersistentDataType.STRING);
            if(id == null) {
                return;
            }
            var entry = Main.getInstance().data().mobs().get(
                    Objects.requireNonNull(NamespacedKey.fromString(id))
            );
            if(entry == null) {
                return;
            }
            entry.executeDrops(event.getEntity().getLocation());
        }
    }
}
