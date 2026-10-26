/**
 * File: ModBlocks.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.furranystudio.noteworthy.registry.ModRegistry;
import org.furranystudio.noteworthy.registry.RegistryEntry;

public final class ModBlocks {

    public static final RegistryEntry<Block> JUKEBOX = ModRegistry.register(Registries.BLOCK, "jukebox",
        key -> new NoteJukeboxBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.DIRT)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 6.0F)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .ignitedByLava()));

    private ModBlocks() {
    }

    public static void init() {
    }
}
