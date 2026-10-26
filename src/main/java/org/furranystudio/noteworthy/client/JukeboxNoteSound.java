/**
 * File: JukeboxNoteSound.java
 * Author: Maxime66410
 * Created: 2026-10-26
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

public class JukeboxNoteSound extends SimpleSoundInstance {

    private final int range;

    public JukeboxNoteSound(Identifier sound, float volume, float pitch, int range, BlockPos pos) {
        super(sound, SoundSource.RECORDS, volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, false);
        this.range = range;
    }

    @Override
    public WeighedSoundEvents getOrResolve(SoundManager soundManager) {
        WeighedSoundEvents events = super.getOrResolve(soundManager);
        if (events != null && sound != null && sound != SoundManager.EMPTY_SOUND) {
            sound = new Sound(sound.getLocation(), sound.getVolume(), sound.getPitch(), sound.getWeight(), sound.getType(),
                sound.shouldStream(), sound.shouldPreload(), range);
        }
        return events;
    }
}
