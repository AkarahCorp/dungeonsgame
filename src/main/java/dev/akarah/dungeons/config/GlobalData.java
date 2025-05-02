package dev.akarah.dungeons.config;

import dev.akarah.dungeons.config.item.CustomItem;
import org.jetbrains.annotations.NotNull;

public record GlobalData(
        DataRegistry<@NotNull CustomItem> items
) {
    public static GlobalData create() {
        return new GlobalData(
            new DataRegistry<>()
        );
    }
}
