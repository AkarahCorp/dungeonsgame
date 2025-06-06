package dev.akarah.dungeons.dungeon;

import dev.akarah.dungeons.Main;
import dev.akarah.dungeons.config.item.CustomItem;
import io.papermc.paper.datacomponent.DataComponentTypes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
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
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import dev.akarah.dungeons.config.mob.CustomMob;

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
        var clickedBlock = event.getClickedBlock();
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && clickedBlock != null
                && clickedBlock.getType().equals(Material.CHEST)) {
            event.setCancelled(true);
            var blockState = clickedBlock.getState();
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
    public void shootBow(PlayerInteractEvent event) {
        try {
            var mainItem = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);

            if((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)
                    && event.getHand() == EquipmentSlot.HAND
                    && Objects.requireNonNull(CustomItem.getItemId(mainItem)).toString().contains("bow")
                    && Main.getInstance().data().statsHolder().getHitCooldown(event.getPlayer()) <= 0) {
                Main.getInstance().data().statsHolder()
                        .setHitCooldown(event.getPlayer(), 10);
                event.getPlayer().launchProjectile(Arrow.class);
            }
        } catch (NullPointerException ignored) {

        }
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
                    le.setNoDamageTicks(0);
                }
            }
            if(attackerEntity != null && hitEntity != null) {
                if(hitEntity instanceof LivingEntity le) {
                    le.damage(damage, attackerEntity);
                    le.setNoDamageTicks(0);
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

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent event) {
        if(event.getItem().hasData(DataComponentTypes.DAMAGE)) {
            var dmg = event.getItem().getData(DataComponentTypes.DAMAGE);
            assert dmg != null;

            var max = event.getItem().getData(DataComponentTypes.MAX_DAMAGE);
            assert max != null;

            var durability = max - dmg;
            if(durability - event.getDamage() <= 1) {
                event.setCancelled(true);
                event.getItem().setData(DataComponentTypes.DAMAGE, 1);
            }
        }
    }

    @EventHandler
    public void playerKillEntity(EntityDeathEvent event) {
        if(event.getEntity().getKiller() != null) {
            var player = event.getEntity().getKiller();

            var id = CustomMob.getEntityId(event.getEntity());
            if(id == null) {
                return;
            }

            var mob = Main.getInstance().data().mobs().get(id);
            if(mob == null) {
                return;
            }

            var sh = Main.getInstance().data().statsHolder();
            sh.setXP(player, sh.getXP(player) + mob.staticRewards().xp());
            sh.setEssence(player, sh.getEssence(player) + mob.staticRewards().essence());
        }
    }
}
