package dev.akarah.dungeons.config.item;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public record StatsObject(Map<String, Double> stats) {
    public static Codec<StatsObject> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).xmap(StatsObject::new, StatsObject::stats);
}
