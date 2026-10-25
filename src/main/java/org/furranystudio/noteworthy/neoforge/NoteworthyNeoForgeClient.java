/**
 * File: NoteworthyNeoForgeClient.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.client.JukeboxHud;
import org.furranystudio.noteworthy.client.NoteworthyClient;

public final class NoteworthyNeoForgeClient {

    private NoteworthyNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus) {
        NoteworthyClient.init();
        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(Noteworthy.id("jukebox"), JukeboxHud::render));
    }
}
