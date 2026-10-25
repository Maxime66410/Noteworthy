/**
 * File: NoteworthyForgeClient.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.forge;

import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.client.JukeboxHud;
import org.furranystudio.noteworthy.client.NoteworthyClient;

public final class NoteworthyForgeClient {

    private NoteworthyForgeClient() {
    }

    public static void init() {
        NoteworthyClient.init();
        AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(Noteworthy.id("jukebox"), JukeboxHud::render));
    }
}
