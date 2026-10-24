/**
 * File: ModItems.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.furranystudio.noteworthy.block.ModBlocks;
import org.furranystudio.noteworthy.registry.ModRegistry;
import org.furranystudio.noteworthy.registry.RegistryEntry;

public final class ModItems {

    public static final RegistryEntry<BlockItem> JUKEBOX = ModRegistry.register(Registries.ITEM, "jukebox",
        key -> new BlockItem(ModBlocks.JUKEBOX.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    private ModItems() {
    }

    public static void init() {
    }
}
