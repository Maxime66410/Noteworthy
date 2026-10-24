/**
 * File: NoteworthyClient.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.Minecraft;
import org.furranystudio.noteworthy.network.ModNetworking;
import org.furranystudio.noteworthy.network.packet.JukeboxStatePayload;
import org.furranystudio.noteworthy.network.packet.OpenJukeboxPayload;

public final class NoteworthyClient {

    private NoteworthyClient() {
    }

    public static void init() {
        ModNetworking.setClientHandler(OpenJukeboxPayload.TYPE, open ->
            Minecraft.getInstance().gui.setScreen(new JukeboxScreen(open.pos(), open.songs(), open.state())));
        ModNetworking.setClientHandler(JukeboxStatePayload.TYPE, state -> {
            if (Minecraft.getInstance().gui.screen() instanceof JukeboxScreen screen && screen.pos().equals(state.pos())) {
                screen.applyState(state);
            }
        });
    }
}
