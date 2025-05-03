package dev.akarah.dungeons.config;

import dev.akarah.dungeons.config.item.CustomItem;
import dev.akarah.dungeons.config.item.PlayerStatsHolder;
import org.jetbrains.annotations.NotNull;

public record GlobalData(
        DataRegistry<@NotNull CustomItem> items,

        PlayerStatsHolder statsHolder
) {
    public static GlobalData create() {
        return new GlobalData(
            new DataRegistry<>(),
                new PlayerStatsHolder()
        );
    }
}
