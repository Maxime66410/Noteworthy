/**
 * File: NoteJukeboxBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.noteworthy.network.ModPackets;
import org.furranystudio.noteworthy.song.Song;
import org.furranystudio.noteworthy.song.Songs;

public class NoteJukeboxBlockEntity extends BlockEntity {

    private static final float VOLUME = 3.0F;
    private static final double PARTICLE_HEIGHT = 1.2;
    private static final double NOTE_COLORS = 24.0;

    private Identifier song;
    private int tick;

    public NoteJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JUKEBOX.get(), pos, state);
    }

    public Identifier song() {
        return song;
    }

    public int tick() {
        return tick;
    }

    public void play(Identifier song) {
        this.song = song;
        tick = 0;
        changed();
    }

    public void stop() {
        song = null;
        tick = 0;
        changed();
    }

    private void changed() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            ModPackets.sendState(serverLevel, this);
        }
    }

    void serverTick(ServerLevel level, BlockPos pos) {
        if (song == null) {
            return;
        }
        Song playing = Songs.find(level.getServer(), song);
        if (playing == null || tick >= playing.length()) {
            stop();
            return;
        }
        for (Song.Part part : playing.parts()) {
            int[] notes = part.notes().get(tick);
            if (notes != null) {
                playNotes(level, pos, part, notes);
            }
        }
        tick++;
        setChanged();
    }

    private static void playNotes(ServerLevel level, BlockPos pos, Song.Part part, int[] notes) {
        boolean tunable = part.instrument().isTunable();
        for (int note : notes) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, part.instrument().getSoundEvent().value(),
                SoundSource.RECORDS, VOLUME, tunable ? NoteBlock.getPitchFromNote(note) : 1.0F);
            if (tunable) {
                level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + PARTICLE_HEIGHT, pos.getZ() + 0.5,
                    0, note / NOTE_COLORS, 0.0, 0.0, 1.0);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (song != null) {
            output.putString("Song", song.toString());
            output.putInt("Tick", tick);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        song = input.getString("Song").map(Identifier::tryParse).orElse(null);
        tick = input.getIntOr("Tick", 0);
    }
}
