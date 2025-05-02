package dev.akarah.dungeons.config;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record DataConfig(
        List<String> items
) {
    public static Codec<DataConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("items").forGetter(DataConfig::items)
    ).apply(instance, DataConfig::new));
}
