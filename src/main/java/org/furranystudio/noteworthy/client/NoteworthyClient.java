/**
 * File: NoteworthyClient.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.furranystudio.noteworthy.block.NoteJukeboxBlockEntity;
import org.furranystudio.noteworthy.network.ModNetworking;
import org.furranystudio.noteworthy.network.packet.JukeboxNotesPayload;
import org.furranystudio.noteworthy.network.packet.JukeboxStatePayload;
import org.furranystudio.noteworthy.network.packet.OpenJukeboxPayload;

public final class NoteworthyClient {

    private NoteworthyClient() {
    }

    public static void init() {
        ModNetworking.setClientHandler(OpenJukeboxPayload.TYPE, open ->
            Minecraft.getInstance().gui.setScreen(new JukeboxScreen(open.pos(), open.songs(), open.state())));
        ModNetworking.setClientHandler(JukeboxStatePayload.TYPE, state -> {
            Screen current = Minecraft.getInstance().gui.screen();
            if (current instanceof JukeboxScreen screen && screen.pos().equals(state.pos())) {
                screen.applyState(state);
            } else if (current instanceof JukeboxSettingsScreen settings && settings.parent().pos().equals(state.pos())) {
                settings.parent().applyState(state);
                settings.refresh();
            }
        });
        ModNetworking.setClientHandler(JukeboxNotesPayload.TYPE, notes -> {
            float volume = notes.volume() / (float) NoteJukeboxBlockEntity.MAX_VOLUME;
            for (JukeboxNotesPayload.Note note : notes.notes()) {
                Minecraft.getInstance().getSoundManager().play(new JukeboxNoteSound(note.sound(), volume, note.pitch(), notes.range(), notes.pos()));
            }
        });
    }
}
