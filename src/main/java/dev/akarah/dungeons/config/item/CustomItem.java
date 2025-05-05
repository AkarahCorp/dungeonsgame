package dev.akarah.dungeons.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.dungeons.Main;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public record CustomItem(
        NamespacedKey key,
        Visuals visuals,
        StatsObject stats,
        Optional<FoodObject> food,
        Optional<Integer> durability
) implements Keyed {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).fieldOf("id").forGetter(CustomItem::key),
            Visuals.CODEC.fieldOf("visuals").forGetter(CustomItem::visuals),
            StatsObject.CODEC.optionalFieldOf("stats", StatsObject.empty()).forGetter(CustomItem::stats),
            FoodObject.CODEC.optionalFieldOf("food").forGetter(CustomItem::food),
            Codec.INT.optionalFieldOf("durability").forGetter(CustomItem::durability)
    ).apply(instance, CustomItem::new));

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public ItemStack toItemStack() {
        var is = ItemStack.of(this.visuals().type());

        setItemId(is, this.key);
        is.setData(DataComponentTypes.ITEM_NAME, Component.text(this.visuals().name()));
        is.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                .addString(this.key.asString())
                .build());
        is.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS).build());

        this.durability.ifPresentOrElse(
                durability -> {
                    is.setData(DataComponentTypes.MAX_DAMAGE, durability);
                    is.setData(DataComponentTypes.DAMAGE, 0);
                },
                () -> {
                    is.unsetData(DataComponentTypes.MAX_DAMAGE);
                    is.unsetData(DataComponentTypes.DAMAGE);
                }
        );

        is.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes()
                .addModifier(Attribute.ATTACK_SPEED,
                        new AttributeModifier(
                                Main.getInstance().createKey("atkspd"),
                                300.0,
                                AttributeModifier.Operation.ADD_NUMBER)).build());
        is.unsetData(DataComponentTypes.BLOCKS_ATTACKS);
        is.unsetData(DataComponentTypes.FOOD);
        is.setData(DataComponentTypes.RARITY, ItemRarity.COMMON);

        if(this.visuals().type().toString().toLowerCase().contains("sword")) {
            is.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks().build());
        }

        this.food.ifPresent(food -> {
            is.setData(DataComponentTypes.CONSUMABLE,
                    Consumable.consumable()
                            .consumeSeconds(food.consumeTime())
                            .build()
            );
            is.setData(DataComponentTypes.FOOD,
                    FoodProperties
                            .food()
                            .canAlwaysEat(true)
                            .saturation(food.saturation())
                            .nutrition(food.hunger())
                            .build()
            );
        });

        if(!this.stats().innerMap().isEmpty()) {
            var lore = new ArrayList<Component>();
            lore.add(Component.text("When equipped:").color(TextColor.color(133, 133, 133)));
            for(var entry : this.stats().innerMap().entrySet()) {
                lore.add(Component.text(" " + entry.getValue() + " " +
                        CustomItem.titleCase(entry.getKey().replace("_", " ")))
                        .color(TextColor.color(0, 133, 255))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                );
            }
            is.setData(DataComponentTypes.LORE, ItemLore.lore()
                    .addLines(lore)
                    .build());
        }


        return is;
    }

    public static void setItemId(ItemStack is, NamespacedKey key) {
        is.editPersistentDataContainer(container ->
                container.set(Main.getInstance().createKey("item/id"), PersistentDataType.STRING, key.toString()));
    }

    public static NamespacedKey getItemId(ItemStack is) {
        var tag = is.getPersistentDataContainer().get(Main.getInstance().createKey("item/id"), PersistentDataType.STRING);
        if(tag == null) {
            tag = "akarahnet:none";
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
            String name
    ) {
        public static Codec<Visuals> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(Material::matchMaterial, Material::name).fieldOf("type").forGetter(Visuals::type),
                Codec.STRING.fieldOf("name").forGetter(Visuals::name)
        ).apply(instance, Visuals::new));
    }

    public record FoodObject(
            int hunger,
            float saturation,
            float consumeTime
    ) {
        public static Codec<FoodObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("hunger", 0).forGetter(FoodObject::hunger),
                Codec.FLOAT.optionalFieldOf("saturation", 0.0f).forGetter(FoodObject::saturation),
                Codec.FLOAT.optionalFieldOf("consume_time", 1.0f).forGetter(FoodObject::consumeTime)
        ).apply(instance, FoodObject::new));
    }
}
