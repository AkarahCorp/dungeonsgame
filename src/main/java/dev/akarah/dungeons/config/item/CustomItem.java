package dev.akarah.dungeons.config.item;

import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.akarah.dungeons.Main;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;

public record CustomItem(
        NamespacedKey key,
        Visuals visuals,
        StatsObject stats,
        Optional<FoodObject> food,
        Optional<Integer> durability) implements Keyed {

    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).fieldOf("id")
                    .forGetter(CustomItem::key),
            Visuals.CODEC.fieldOf("visuals").forGetter(CustomItem::visuals),
            StatsObject.CODEC.optionalFieldOf("stats", StatsObject.empty()).forGetter(CustomItem::stats),
            FoodObject.CODEC.optionalFieldOf("food").forGetter(CustomItem::food),
            Codec.INT.optionalFieldOf("durability").forGetter(CustomItem::durability))
            .apply(instance, CustomItem::new));

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public ItemStack toItemStack() {
        var itemStack = ItemStack.of(this.visuals().type());
        if (this.visuals().type().equals(Material.BOW)) {
            itemStack = itemStack.withType(Material.POISONOUS_POTATO);
            itemStack.setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft:bow"));
        }

        setItemId(itemStack, this.key);
        itemStack.setData(DataComponentTypes.ITEM_NAME, Component.text(this.visuals().name()));
        itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                .addString(this.key.asString())
                .build());
        itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS).build());

        var finalItemStack = itemStack;
        this.durability.ifPresentOrElse(
                durabilityAmount -> {
                    finalItemStack.setData(DataComponentTypes.MAX_DAMAGE, durabilityAmount);
                    finalItemStack.setData(DataComponentTypes.DAMAGE, 0);
                },
                () -> {
                    finalItemStack.unsetData(DataComponentTypes.MAX_DAMAGE);
                    finalItemStack.unsetData(DataComponentTypes.DAMAGE);
                });

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes()
                .addModifier(Attribute.ATTACK_SPEED,
                        new AttributeModifier(
                                Main.getInstance().createKey("atkspd"),
                                300.0,
                                AttributeModifier.Operation.ADD_NUMBER))
                .build());
        itemStack.unsetData(DataComponentTypes.BLOCKS_ATTACKS);
        itemStack.unsetData(DataComponentTypes.FOOD);
        itemStack.setData(DataComponentTypes.RARITY, ItemRarity.COMMON);

        if (this.visuals().type().toString().toLowerCase().contains("sword")) {
            itemStack.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks().build());
        }

        this.food.ifPresentOrElse(foodObject -> {
            finalItemStack.setData(DataComponentTypes.CONSUMABLE,
                    Consumable.consumable()
                            .consumeSeconds(foodObject.consumeTime())
                            .build());
            finalItemStack.setData(DataComponentTypes.FOOD,
                    FoodProperties
                            .food()
                            .canAlwaysEat(true)
                            .saturation(foodObject.saturation())
                            .nutrition(foodObject.hunger())
                            .build());
        }, () -> {
            finalItemStack.unsetData(DataComponentTypes.FOOD);
            finalItemStack.unsetData(DataComponentTypes.CONSUMABLE);
        });

        var lore = new ArrayList<Component>();

        if (!this.stats().innerMap().isEmpty()) {
            lore.add(Component.text("Gear Score: ").color(TextColor.color(133, 133, 133))
                    .append(Component.text(this.gearScore()).color(TextColor.color(255, 0, 255))));
            lore.add(Component.empty());
            lore.add(Component.text("When equipped:").color(TextColor.color(133, 133, 133)));
            for (var entry : this.stats().innerMap().entrySet()) {
                lore.add(Component.text(" " + entry.getValue() + " " +
                        CustomItem.titleCase(entry.getKey().replace("_", " ")))
                        .color(TextColor.color(0, 133, 255))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
        }

        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore()
                .addLines(lore)
                .build());

        itemStack.unsetData(DataComponentTypes.TOOL);
        itemStack.setData(DataComponentTypes.TOOL,
                Tool.tool()
                        .damagePerBlock(1)
                        .addRule(Tool.rule(
                                RegistrySet.keySet(RegistryKey.BLOCK,
                                        TypedKey.create(RegistryKey.BLOCK, Key.key("minecraft:coal_block"))),
                                4f,
                                TriState.TRUE))
                        .build());

        return itemStack;
    }

    public static void setItemId(ItemStack is, NamespacedKey key) {
        is.editPersistentDataContainer(container -> container.set(Main.getInstance().createKey("item/id"),
                PersistentDataType.STRING, key.toString()));
    }

    public static NamespacedKey getItemId(ItemStack is) {
        var tag = is.getPersistentDataContainer().get(Main.getInstance().createKey("item/id"),
                PersistentDataType.STRING);
        if (tag == null) {
            return null;
        }
        return NamespacedKey.fromString(tag);
    }

    public static String titleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

    public record Visuals(
            Material type,
            String name) {
        public static Codec<Visuals> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(Material::matchMaterial, Material::name).fieldOf("type").forGetter(Visuals::type),
                Codec.STRING.fieldOf("name").forGetter(Visuals::name)).apply(instance, Visuals::new));
    }

    public record FoodObject(
            int hunger,
            float saturation,
            float consumeTime) {
        public static Codec<FoodObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("hunger", 0).forGetter(FoodObject::hunger),
                Codec.FLOAT.optionalFieldOf("saturation", 0.0f).forGetter(FoodObject::saturation),
                Codec.FLOAT.optionalFieldOf("consume_time", 1.0f).forGetter(FoodObject::consumeTime))
                .apply(instance, FoodObject::new));
    }

    public int gearScore() {
        return (int) ((this.stats.get(Stats.MAX_HEALTH) * 12)
                + (this.stats.get(Stats.ARMOR) * 10)
                + (this.stats.get(Stats.ATTACK_DAMAGE) * 6)
                + (this.stats.get(Stats.ATTACK_SPEED) * 6)
                + (this.stats.get(Stats.WALK_SPEED) * 12));
    }
}
