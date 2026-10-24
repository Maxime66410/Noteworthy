/**
 * File: ModRegistry.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.furranystudio.noteworthy.Noteworthy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModRegistry {

    // Matches RegisterEvent.register on Forge and NeoForge
    public interface Sink {
        <T> void register(ResourceKey<? extends Registry<T>> registry, Identifier id, Supplier<T> value);
    }

    private static final List<Entry<?, ?>> ENTRIES = new ArrayList<>();
    private static boolean registered;

    private ModRegistry() {
    }

    public static <T, R extends T> RegistryEntry<R> register(ResourceKey<? extends Registry<T>> registry, String name,
                                                             Function<ResourceKey<T>, R> factory) {
        if (registered) {
            throw new IllegalStateException("Too late to register " + name + ", registries are already filled");
        }
        ResourceKey<T> key = ResourceKey.create(registry, Noteworthy.id(name));
        RegistryEntry<R> entry = new RegistryEntry<>(key);
        ENTRIES.add(new Entry<>(registry, key, factory, entry));
        return entry;
    }

    public static void registerAll(Sink sink) {
        registered = true;
        for (Entry<?, ?> entry : ENTRIES) {
            entry.pushTo(sink);
        }
    }

    private record Entry<T, R extends T>(ResourceKey<? extends Registry<T>> registry, ResourceKey<T> key,
                                         Function<ResourceKey<T>, R> factory, RegistryEntry<R> holder) {

        void pushTo(Sink sink) {
            sink.register(registry, key.identifier(), () -> {
                R value = factory.apply(key);
                holder.bind(value);
                Noteworthy.LOGGER.info("[Noteworthy] Registered {} in {}", key.identifier(), registry.identifier());
                return value;
            });
        }
    }
}
