/**
 * File: ModBlockEntities.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.furranystudio.noteworthy.registry.ModRegistry;
import org.furranystudio.noteworthy.registry.RegistryEntry;

import java.util.Set;

public final class ModBlockEntities {

    public static final RegistryEntry<BlockEntityType<NoteJukeboxBlockEntity>> JUKEBOX = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "jukebox",
        key -> new BlockEntityType<>(NoteJukeboxBlockEntity::new, Set.of(ModBlocks.JUKEBOX.get())));

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
