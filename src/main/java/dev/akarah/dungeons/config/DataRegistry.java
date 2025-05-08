package dev.akarah.dungeons.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Iterators;

import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;

public class DataRegistry<T extends Keyed> {
    Map<NamespacedKey, T> keyToT = new HashMap<>();

    public @Nullable T get(@NotNull NamespacedKey key) {
        return keyToT.get(key);
    }

    public @Nullable NamespacedKey getKey(T value) {
        return value.getKey();
    }

    public boolean hasTag(@NotNull TagKey<T> key) {
        return false;
    }

    public @NotNull Tag<@NotNull T> getTag(@NotNull TagKey<T> key) {
        throw new UnsupportedOperationException("nope");
    }

    public @NotNull Collection<Tag<@NotNull T>> getTags() {
        return List.of();
    }

    public @NotNull Stream<T> stream() {
        return Stream.empty();
    }

    public @NotNull Stream<NamespacedKey> keyStream() {
        return Stream.empty();
    }

    public int size() {
        return 0;
    }

    public @NotNull Iterator<T> iterator() {
        return Iterators.forArray();
    }

    public DataRegistry<T> register(NamespacedKey key, T value) {
        this.keyToT.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return this.keyToT.toString();
    }
}
