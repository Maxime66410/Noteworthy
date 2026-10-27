/**
 * File: MenuBands.java
 * Author: Maxime66410
 * Created: 2026-10-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class MenuBands {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/block/note_block.png");
    private static final int TEXTURE_SIZE = 16;
    private static final int BORDER = 2;
    private static final int INNER_SIZE = TEXTURE_SIZE - BORDER * 2;
    private static final int TILE = INNER_SIZE * 2;
    private static final int SHADE = 0xFF404040;
    private static final int SEPARATOR_HEIGHT = 2;

    private MenuBands() {
    }

    public static void extract(GuiGraphicsExtractor graphics, int width, int height, int headerHeight, int footerHeight) {
        extractBand(graphics, 0, width, headerHeight);
        extractBand(graphics, height - footerHeight, width, footerHeight);
    }

    public static void extractSeparators(GuiGraphicsExtractor graphics, int width, int height, int headerHeight, int footerHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.INWORLD_HEADER_SEPARATOR, 0, headerHeight - SEPARATOR_HEIGHT,
            0.0F, 0.0F, width, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.INWORLD_FOOTER_SEPARATOR, 0, height - footerHeight,
            0.0F, 0.0F, width, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    }

    private static void extractBand(GuiGraphicsExtractor graphics, int y, int width, int height) {
        if (height <= 0) {
            return;
        }
        graphics.enableScissor(0, y, width, y + height);
        for (int tileY = y; tileY < y + height; tileY += TILE) {
            for (int tileX = 0; tileX < width; tileX += TILE) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, tileX, tileY, BORDER, BORDER,
                    TILE, TILE, INNER_SIZE, INNER_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, SHADE);
            }
        }
        graphics.disableScissor();
    }
}
