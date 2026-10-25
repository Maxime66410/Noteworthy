/**
 * File: NoteworthyFabricClient.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.client.JukeboxHud;
import org.furranystudio.noteworthy.client.NoteworthyClient;

public final class NoteworthyFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricNetwork.registerClient();
        NoteworthyClient.init();
        HudElementRegistry.addLast(Noteworthy.id("jukebox"), JukeboxHud::render);
    }
}
