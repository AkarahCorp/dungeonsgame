package dev.akarah.dungeons.config.mob;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.dungeons.Main;
import dev.akarah.dungeons.config.item.Stats;
import dev.akarah.dungeons.config.item.StatsObject;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record CustomMob(
        NamespacedKey id,
        StatsObject stats,
        Visuals visuals,
        Optional<Equipment> equipment,
        List<DropEntry> drops
) implements Keyed {
    public static Codec<CustomMob> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).fieldOf("id").forGetter(CustomMob::id),
            StatsObject.CODEC.fieldOf("stats").forGetter(CustomMob::stats),
            Visuals.CODEC.fieldOf("visuals").forGetter(CustomMob::visuals),
            Equipment.CODEC.optionalFieldOf("equipment").forGetter(CustomMob::equipment),
            DropEntry.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(CustomMob::drops)
    ).apply(instance, CustomMob::new));

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.id;
    }

    public void spawn(Location location) {
        var et = EntityType.fromName(this.visuals().entityType().asMinimalString());
        if(et == null) {
            System.out.println(this.visuals.entityType().asMinimalString() + " is not a valid EntityType");
            return;
        }
        var entity = location.getWorld().spawnEntity(
                location,
                et
        );
        entity.setCustomNameVisible(true);
        entity.customName(Component.text(this.visuals().name()));
        entity.getPersistentDataContainer()
                .set(Main.getInstance().createKey("entity/id"), PersistentDataType.STRING, this.id.toString());
        if(entity instanceof LivingEntity le) {
            Objects.requireNonNull(le.getEquipment()).clear();

            if(entity instanceof Ageable ageable) {
                ageable.setAdult();
                ageable.setAge(2);
            }

            le.registerAttribute(Attribute.MAX_HEALTH);
            Objects.requireNonNull(le.getAttribute(Attribute.MAX_HEALTH))
                    .setBaseValue(this.stats().get(Stats.MAX_HEALTH));

            le.registerAttribute(Attribute.ATTACK_DAMAGE);
            Objects.requireNonNull(le.getAttribute(Attribute.ATTACK_DAMAGE))
                    .setBaseValue(this.stats.get(Stats.ATTACK_DAMAGE));

            this.equipment().ifPresent(equipment -> {
                equipment.head().ifPresent(item -> applyEquipment(le, EquipmentSlot.HEAD, item));
                equipment.body.ifPresent(item -> applyEquipment(le, EquipmentSlot.BODY, item));
                equipment.legs.ifPresent(item -> applyEquipment(le, EquipmentSlot.LEGS, item));
                equipment.feet.ifPresent(item -> applyEquipment(le, EquipmentSlot.FEET, item));
                equipment.mainHand.ifPresent(item -> applyEquipment(le, EquipmentSlot.HAND, item));
                equipment.offHand.ifPresent(item -> applyEquipment(le, EquipmentSlot.OFF_HAND, item));
            });
        }
    }

    public static void applyEquipment(Entity entity, EquipmentSlot slot, NamespacedKey key) {
        var item = Main.getInstance().data().items().get(key);
        if(item == null) {
            return;
        }

        if(entity instanceof LivingEntity le) {
            Objects.requireNonNull(le.getEquipment()).setItem(slot, item.toItemStack());
        }
    }

    record Visuals(
            String name,
            NamespacedKey entityType
    ) {
        public static Codec<Visuals> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Visuals::name),
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).fieldOf("type").forGetter(Visuals::entityType)
        ).apply(instance, Visuals::new));
    }

    record Equipment(
            Optional<NamespacedKey> head,
            Optional<NamespacedKey> body,
            Optional<NamespacedKey> legs,
            Optional<NamespacedKey> feet,
            Optional<NamespacedKey> mainHand,
            Optional<NamespacedKey> offHand
    ) {
        public static Codec<Equipment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).optionalFieldOf("head")
                        .forGetter(Equipment::head),
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).optionalFieldOf("body")
                        .forGetter(Equipment::body),
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).optionalFieldOf("legs")
                        .forGetter(Equipment::legs),
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).optionalFieldOf("feet")
                        .forGetter(Equipment::feet),
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).optionalFieldOf("main_hand")
                        .forGetter(Equipment::mainHand),
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).optionalFieldOf("off_hand")
                        .forGetter(Equipment::offHand)
        ).apply(instance, Equipment::new));
    }

    record DropEntry(
            List<NamespacedKey> item,
            double chance,
            int amount
    ) {
        public static Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString)
                        .listOf().fieldOf("item").forGetter(DropEntry::item),
                Codec.DOUBLE.fieldOf("chance").forGetter(DropEntry::chance),
                Codec.INT.optionalFieldOf("amount", 1).forGetter(DropEntry::amount)
        ).apply(instance, DropEntry::new));
    }

    public void executeDrops(Location location) {
        for(var entry : this.drops) {
            var rng = Math.random() * 100;
            if(rng <= entry.chance()) {
                var itemSelection = entry.item().get(new Random().nextInt(entry.item.size()));
                var customItem = Main.getInstance().data().items().get(itemSelection);
                if(customItem == null) {
                    continue;
                }
                location.getWorld().dropItem(
                        location,
                        customItem.toItemStack()
                                .add(entry.amount() - 1)
                );
            }
        }
    }
}
