package dev.akarah.dungeons.config;

import dev.akarah.dungeons.config.item.CustomItem;
import dev.akarah.dungeons.config.item.PlayerStatsHolder;
import dev.akarah.dungeons.config.mob.CustomMob;
import org.jetbrains.annotations.NotNull;

public record GlobalData(
        DataRegistry<@NotNull CustomItem> items,
        DataRegistry<@NotNull CustomMob> mobs,

        PlayerStatsHolder statsHolder
) {
    public static GlobalData create() {
        return new GlobalData(
                new DataRegistry<>(),
                new DataRegistry<>(),
                new PlayerStatsHolder()
        );
    }
}
