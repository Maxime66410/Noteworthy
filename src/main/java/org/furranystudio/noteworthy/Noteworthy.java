/**
 * File: Noteworthy.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.furranystudio.noteworthy.block.ModBlockEntities;
import org.furranystudio.noteworthy.block.ModBlocks;
import org.furranystudio.noteworthy.item.ModItems;
import org.furranystudio.noteworthy.network.ModPackets;
import org.slf4j.Logger;

public final class Noteworthy {

    public static final String MODID = "noteworthy";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Noteworthy() {
    }

    public static void registerContent() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModPackets.init();
    }

    public static void commonSetup() {
        LOGGER.info("[Noteworthy] Common setup done.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
