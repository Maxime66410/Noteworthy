/**
 * File: RegistryEntry.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class RegistryEntry<T> implements Supplier<T> {

    private final ResourceKey<? super T> key;
    private T value;

    RegistryEntry(ResourceKey<? super T> key) {
        this.key = key;
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("Registry entry " + key + " used before it was registered");
        }
        return value;
    }

    public Identifier id() {
        return key.identifier();
    }

    public boolean isBound() {
        return value != null;
    }

    void bind(T value) {
        this.value = value;
    }
}
