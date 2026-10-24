/**
 * File: NoteworthyFabricClient.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.furranystudio.noteworthy.client.NoteworthyClient;

public final class NoteworthyFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricNetwork.registerClient();
        NoteworthyClient.init();
    }
}
