package dev.akarah.dungeons.config.item;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public class StatsObject {
    Map<String, Double> stats = new HashMap<>();

    public static Codec<StatsObject> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).xmap(StatsObject::fromMap, StatsObject::innerMap);

    public static StatsObject fromMap(Map<String, Double> map) {
        var o = new StatsObject();
        o.stats = map;
        return o;
    }

    public static StatsObject empty() {
        return new StatsObject();
    }

    public static StatsObject baseStats() {
        return new StatsObject()
                .with(Stats.MAX_HEALTH, 20)
                .with(Stats.ATTACK_SPEED, 2);
    }

    public double get(String key) {
        return this.stats.getOrDefault(key, 0.0);
    }

    public StatsObject with(String key, double value) {
        this.stats.put(key, value);
        return this;
    }

    public StatsObject add(StatsObject other) {
        var so = new StatsObject();
        var keySet = Sets.union(this.stats.keySet(), other.stats.keySet());
        for(var key : keySet) {
            so.with(key, this.get(key) + other.get(key));
        }
        return so;
    }

    public Map<String, Double> innerMap() {
        return this.stats;
    }
}
