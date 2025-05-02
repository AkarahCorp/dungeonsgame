package dev.akarah.dungeons.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.dungeons.Main;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record CustomItem(
        NamespacedKey key,
        Visuals visuals,
        StatsObject stats
) implements Keyed {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(NamespacedKey::fromString, NamespacedKey::asString).fieldOf("id").forGetter(CustomItem::key),
            Visuals.CODEC.fieldOf("visuals").forGetter(CustomItem::visuals),
            StatsObject.CODEC.optionalFieldOf("stats", new StatsObject(new HashMap<>())).forGetter(CustomItem::stats)
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

    public record Visuals(
            Material type,
            String name
    ) {
        public static Codec<Visuals> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(Material::matchMaterial, Material::name).fieldOf("type").forGetter(Visuals::type),
                Codec.STRING.fieldOf("name").forGetter(Visuals::name)
        ).apply(instance, Visuals::new));
    }
}
