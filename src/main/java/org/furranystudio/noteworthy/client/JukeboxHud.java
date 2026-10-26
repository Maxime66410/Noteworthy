/**
 * File: JukeboxHud.java
 * Author: Maxime66410
 * Created: 2026-10-25
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.furranystudio.noteworthy.block.NoteJukeboxBlockEntity;
import org.furranystudio.noteworthy.song.Songs;

public final class JukeboxHud {

    private static final int CROSSHAIR_OFFSET = 12;
    private static final int TITLE_COLOR = 0xFFFFFF55;
    private static final int TIME_COLOR = 0xFFA0A0A0;
    private static final String PLAYING_MARK = "♪ ";
    private static final String PAUSED_MARK = "⏸ ";

    private JukeboxHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gui.screen() != null || minecraft.gui.hud.isHidden()) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof NoteJukeboxBlockEntity jukebox)
                || jukebox.song() == null || jukebox.title().isEmpty()) {
            return;
        }
        Font font = minecraft.font;
        String line = jukebox.author().isEmpty() ? jukebox.title() : jukebox.title() + " - " + jukebox.author();
        String time = Songs.formatTime(Math.min(jukebox.tick(), jukebox.length())) + " / " + Songs.formatTime(jukebox.length());
        int x = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + CROSSHAIR_OFFSET;
        graphics.centeredText(font, (jukebox.paused() ? PAUSED_MARK : PLAYING_MARK) + line, x, y, TITLE_COLOR);
        graphics.centeredText(font, time, x, y + font.lineHeight + 1, TIME_COLOR);
    }
}
