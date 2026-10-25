/**
 * File: JukeboxScreen.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.furranystudio.noteworthy.network.ModNetworking;
import org.furranystudio.noteworthy.network.packet.JukeboxStatePayload;
import org.furranystudio.noteworthy.network.packet.PlayJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.SongEntry;
import org.furranystudio.noteworthy.network.packet.StopJukeboxPayload;

import java.util.List;
import java.util.Locale;

public class JukeboxScreen extends Screen {

    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_HEIGHT = 60;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_WIDTH = 260;
    private static final int SEARCH_WIDTH = 200;
    private static final int SEARCH_HEIGHT = 15;
    private static final int PROGRESS_WIDTH = 200;
    private static final int PROGRESS_HEIGHT = 18;
    private static final int BAR_HEIGHT = 4;
    private static final int TICKS_PER_SECOND = 20;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int PLAYING_COLOR = 0xFFFFFF55;
    private static final int MUTED_COLOR = 0xFFA0A0A0;
    private static final int BAR_BACK = 0xFF404040;
    private static final int BAR_FILL = 0xFF55FF55;
    private static final int PLAY_SIZE = 16;
    private static final int BUTTON_FILL = 0xFF3A3A3A;
    private static final int BUTTON_BORDER = 0xFF808080;
    private static final int BUTTON_BORDER_HOVER = 0xFFFFFFFF;
    private static final int PLAY_ICON_HALF = 4;
    private static final int STOP_ICON_SIZE = 8;
    private static final String PLAYING_MARK = "♪ ";

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, HEADER_HEIGHT, FOOTER_HEIGHT);
    private final BlockPos pos;
    private final List<SongEntry> songs;
    private Identifier playing;
    private int tick;
    private SongList list;
    private Button stop;
    private SpacerElement progress;

    public JukeboxScreen(BlockPos pos, List<SongEntry> songs, JukeboxStatePayload state) {
        super(Component.translatable("block.noteworthy.jukebox"));
        this.pos = pos;
        this.songs = songs;
        applyState(state);
    }

    public BlockPos pos() {
        return pos;
    }

    public void applyState(JukeboxStatePayload state) {
        playing = state.song().orElse(null);
        tick = state.tick();
        refresh();
    }

    @Override
    protected void init() {
        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(title, font));
        Component searchHint = Component.translatable("noteworthy.jukebox.search");
        EditBox search = header.addChild(new EditBox(font, SEARCH_WIDTH, SEARCH_HEIGHT, searchHint));
        search.setHint(searchHint.copy().withStyle(EditBox.SEARCH_HINT_STYLE));
        search.setResponder(text -> list.filter(text));

        list = layout.addToContents(new SongList(minecraft));

        LinearLayout footer = layout.addToFooter(LinearLayout.vertical().spacing(6));
        footer.defaultCellSetting().alignHorizontallyCenter();
        progress = footer.addChild(new SpacerElement(PROGRESS_WIDTH, PROGRESS_HEIGHT));
        LinearLayout buttons = footer.addChild(LinearLayout.horizontal().spacing(8));
        stop = buttons.addChild(Button.builder(Component.translatable("noteworthy.jukebox.stop"),
            button -> ModNetworking.sendToServer(new StopJukeboxPayload(pos))).build());
        buttons.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
        setInitialFocus(search);
        refresh();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) {
            list.updateSize(width, layout);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        SongEntry current = current();
        if (current == null) {
            return;
        }
        tick++;
        if (tick >= current.length()) {
            playing = null;
            refresh();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (list.children().isEmpty()) {
            Component empty = Component.translatable(songs.isEmpty() ? "noteworthy.jukebox.empty" : "noteworthy.jukebox.no_results");
            graphics.centeredText(font, empty, width / 2, layout.getHeaderHeight() + layout.getContentHeight() / 2 - font.lineHeight / 2, MUTED_COLOR);
        }
        extractProgress(graphics);
    }

    private void extractProgress(GuiGraphicsExtractor graphics) {
        int x = progress.getX();
        int y = progress.getY();
        int center = x + PROGRESS_WIDTH / 2;
        SongEntry current = current();
        if (current == null) {
            graphics.centeredText(font, Component.translatable("noteworthy.jukebox.idle"), center, y + (PROGRESS_HEIGHT - font.lineHeight) / 2, MUTED_COLOR);
            return;
        }
        String time = "  " + formatTime(Math.min(tick, current.length())) + " / " + formatTime(current.length());
        String title = font.plainSubstrByWidth(current.title(), PROGRESS_WIDTH - font.width(time));
        graphics.centeredText(font, title + time, center, y, TITLE_COLOR);
        int barY = y + PROGRESS_HEIGHT - BAR_HEIGHT;
        int filled = PROGRESS_WIDTH * Math.min(tick, current.length()) / Math.max(current.length(), 1);
        graphics.fill(x, barY, x + PROGRESS_WIDTH, barY + BAR_HEIGHT, BAR_BACK);
        graphics.fill(x, barY, x + filled, barY + BAR_HEIGHT, BAR_FILL);
    }

    private SongEntry current() {
        if (playing == null) {
            return null;
        }
        return songs.stream().filter(song -> song.id().equals(playing)).findFirst().orElse(null);
    }

    private void refresh() {
        if (stop != null) {
            stop.active = playing != null;
        }
    }

    static String formatTime(int ticks) {
        int seconds = ticks / TICKS_PER_SECOND;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    private class SongList extends ObjectSelectionList<SongList.Entry> {

        SongList(Minecraft minecraft) {
            super(minecraft, JukeboxScreen.this.width, layout.getContentHeight(), layout.getHeaderHeight(), ROW_HEIGHT);
            filter("");
            select(playing);
        }

        void filter(String text) {
            Identifier selected = getSelected() == null ? null : getSelected().song.id();
            String query = text.toLowerCase(Locale.ROOT);
            replaceEntries(songs.stream()
                .filter(song -> query.isEmpty()
                    || song.title().toLowerCase(Locale.ROOT).contains(query)
                    || song.author().toLowerCase(Locale.ROOT).contains(query)
                    || song.id().toString().contains(query))
                .map(Entry::new)
                .toList());
            refreshScrollAmount();
            select(selected);
        }

        private void select(Identifier id) {
            setSelected(children().stream().filter(entry -> entry.song.id().equals(id)).findFirst().orElse(null));
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {

            private final SongEntry song;

            Entry(SongEntry song) {
                this.song = song;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                int buttonX = playButtonX();
                int buttonY = playButtonY();
                String duration = formatTime(song.length());
                int durationX = buttonX - 6 - font.width(duration);
                int textX = getContentX() + 4;
                int textWidth = durationX - textX - 6;
                boolean isPlaying = song.id().equals(playing);
                String title = isPlaying ? PLAYING_MARK + song.title() : song.title();
                int color = isPlaying ? PLAYING_COLOR : TITLE_COLOR;
                if (song.author().isEmpty()) {
                    graphics.text(font, font.plainSubstrByWidth(title, textWidth), textX, getContentYMiddle() - font.lineHeight / 2, color);
                } else {
                    graphics.text(font, font.plainSubstrByWidth(title, textWidth), textX, getContentY() + 2, color);
                    graphics.text(font, font.plainSubstrByWidth(song.author(), textWidth), textX, getContentY() + 12, MUTED_COLOR);
                }
                graphics.text(font, duration, durationX, getContentYMiddle() - font.lineHeight / 2, MUTED_COLOR);

                if (isPlaying || hovered || getSelected() == this) {
                    boolean overButton = insidePlayButton(mouseX, mouseY);
                    graphics.fill(buttonX, buttonY, buttonX + PLAY_SIZE, buttonY + PLAY_SIZE, overButton ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
                    graphics.fill(buttonX + 1, buttonY + 1, buttonX + PLAY_SIZE - 1, buttonY + PLAY_SIZE - 1, BUTTON_FILL);
                    if (isPlaying) {
                        extractStopIcon(graphics, buttonX, buttonY);
                    } else {
                        extractPlayIcon(graphics, buttonX, buttonY);
                    }
                }
            }

            private void extractPlayIcon(GuiGraphicsExtractor graphics, int buttonX, int buttonY) {
                int x = buttonX + (PLAY_SIZE - PLAY_ICON_HALF) / 2;
                int y = buttonY + (PLAY_SIZE - PLAY_ICON_HALF * 2 - 1) / 2;
                for (int row = 0; row <= PLAY_ICON_HALF * 2; row++) {
                    int width = PLAY_ICON_HALF + 1 - Math.abs(row - PLAY_ICON_HALF);
                    graphics.fill(x, y + row, x + width, y + row + 1, TITLE_COLOR);
                }
            }

            private void extractStopIcon(GuiGraphicsExtractor graphics, int buttonX, int buttonY) {
                int offset = (PLAY_SIZE - STOP_ICON_SIZE) / 2;
                graphics.fill(buttonX + offset, buttonY + offset, buttonX + offset + STOP_ICON_SIZE, buttonY + offset + STOP_ICON_SIZE, TITLE_COLOR);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                setSelected(this);
                if (doubleClick || insidePlayButton(event.x(), event.y())) {
                    toggle();
                }
                return true;
            }

            @Override
            public boolean keyPressed(KeyEvent event) {
                if (event.isSelection()) {
                    toggle();
                    return true;
                }
                return super.keyPressed(event);
            }

            private void toggle() {
                if (song.id().equals(playing)) {
                    ModNetworking.sendToServer(new StopJukeboxPayload(pos));
                } else {
                    ModNetworking.sendToServer(new PlayJukeboxPayload(pos, song.id()));
                }
            }

            private int playButtonX() {
                return getContentRight() - 2 - PLAY_SIZE;
            }

            private int playButtonY() {
                return getContentYMiddle() - PLAY_SIZE / 2;
            }

            private boolean insidePlayButton(double x, double y) {
                int buttonX = playButtonX();
                int buttonY = playButtonY();
                return x >= buttonX && x < buttonX + PLAY_SIZE && y >= buttonY && y < buttonY + PLAY_SIZE;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", song.title());
            }
        }
    }
}
