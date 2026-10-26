/**
 * File: JukeboxScreen.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.furranystudio.noteworthy.network.ModNetworking;
import org.furranystudio.noteworthy.network.packet.JukeboxStatePayload;
import org.furranystudio.noteworthy.network.packet.ModeJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.PauseJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.PlayJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.SeekJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.SongEntry;
import org.furranystudio.noteworthy.network.packet.SortJukeboxPayload;
import org.furranystudio.noteworthy.song.PlayMode;
import org.furranystudio.noteworthy.song.SongSort;
import org.furranystudio.noteworthy.network.packet.StopJukeboxPayload;
import org.furranystudio.noteworthy.song.Songs;

import java.util.List;
import java.util.Locale;

public class JukeboxScreen extends Screen {

    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_HEIGHT = 84;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_WIDTH = 260;
    private static final int SEARCH_WIDTH = 156;
    private static final int SORT_WIDTH = 100;
    private static final int STOP_WIDTH = 100;
    private static final int MODE_WIDTH = 130;
    private static final int SEARCH_HEIGHT = 15;
    private static final int PROGRESS_WIDTH = 200;
    private static final int PROGRESS_HEIGHT = 18;
    private static final int BAR_HEIGHT = 6;
    private static final int BAR_GRAB = 2;
    private static final int LABEL_BACK = 0xC0000000;
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
    private static final int PAUSE_ICON_HEIGHT = 8;
    private static final int PAUSE_BAR_WIDTH = 3;
    private static final int PAUSE_GAP = 2;
    private static final String PLAYING_MARK = "♪ ";

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, HEADER_HEIGHT, FOOTER_HEIGHT);
    private final BlockPos pos;
    private final List<SongEntry> songs;
    private Identifier playing;
    private int tick;
    private boolean paused;
    private SongSort sort = SongSort.TITLE;
    private PlayMode mode = PlayMode.ONCE;
    private String query = "";
    private SongList list;
    private Button sortButton;
    private Button modeButton;
    private Button stop;
    private ProgressBar progress;

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
        paused = state.paused();
        SongSort previous = sort;
        sort = state.sort();
        mode = state.mode();
        refresh();
        if (list != null && previous != sort) {
            list.filter(query);
        }
    }

    @Override
    protected void init() {
        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(title, font));
        Component searchHint = Component.translatable("noteworthy.jukebox.search");
        LinearLayout searchRow = header.addChild(LinearLayout.horizontal().spacing(4));
        searchRow.defaultCellSetting().alignVerticallyMiddle();
        EditBox search = searchRow.addChild(new EditBox(font, SEARCH_WIDTH, SEARCH_HEIGHT, searchHint));
        search.setHint(searchHint.copy().withStyle(EditBox.SEARCH_HINT_STYLE));
        search.setResponder(text -> list.filter(text));
        sortButton = searchRow.addChild(Button.builder(sortLabel(),
            button -> ModNetworking.sendToServer(new SortJukeboxPayload(pos, sort.next()))).width(SORT_WIDTH).build());

        list = layout.addToContents(new SongList(minecraft));

        LinearLayout footer = layout.addToFooter(LinearLayout.vertical().spacing(6));
        footer.defaultCellSetting().alignHorizontallyCenter();
        progress = footer.addChild(new ProgressBar());
        LinearLayout controls = footer.addChild(LinearLayout.horizontal().spacing(4));
        stop = controls.addChild(Button.builder(Component.translatable("noteworthy.jukebox.stop"),
            button -> ModNetworking.sendToServer(new StopJukeboxPayload(pos))).width(STOP_WIDTH).build());
        modeButton = controls.addChild(Button.builder(modeLabel(),
            button -> ModNetworking.sendToServer(new ModeJukeboxPayload(pos, mode.next()))).width(MODE_WIDTH).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());

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
        if (current == null || paused) {
            return;
        }
        if (tick < current.length()) {
            tick++;
        }
        if (tick >= current.length() && mode == PlayMode.ONCE) {
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
            progress.active = playing != null;
            sortButton.setMessage(sortLabel());
            modeButton.setMessage(modeLabel());
        }
    }

    private Component modeLabel() {
        return Component.translatable("noteworthy.jukebox.mode", mode.displayName());
    }

    private Component sortLabel() {
        return Component.translatable("noteworthy.jukebox.sort", sort.displayName());
    }

    private class ProgressBar extends AbstractWidget {

        private Identifier dragSong;
        private int dragTick = -1;

        ProgressBar() {
            super(0, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT, Component.empty());
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            SongEntry current = current();
            if (current == null || !current.id().equals(dragSong)) {
                dragTick = -1;
            }
            if (current == null) {
                graphics.centeredText(font, Component.translatable("noteworthy.jukebox.idle"), getX() + PROGRESS_WIDTH / 2,
                    getY() + (PROGRESS_HEIGHT - font.lineHeight) / 2, MUTED_COLOR);
                return;
            }
            int shown = dragTick >= 0 ? dragTick : Math.min(tick, current.length());
            String time = "  " + Songs.formatTime(shown) + " / " + Songs.formatTime(current.length());
            String title = font.plainSubstrByWidth(current.title(), PROGRESS_WIDTH - font.width(time));
            graphics.centeredText(font, title + time, getX() + PROGRESS_WIDTH / 2, getY(), TITLE_COLOR);
            int barY = barY();
            int filled = PROGRESS_WIDTH * shown / Math.max(current.length(), 1);
            graphics.fill(getX(), barY, getX() + PROGRESS_WIDTH, barY + BAR_HEIGHT, BAR_BACK);
            graphics.fill(getX(), barY, getX() + filled, barY + BAR_HEIGHT, BAR_FILL);
            if (dragTick >= 0 || isMouseOver(mouseX, mouseY)) {
                String label = Songs.formatTime(dragTick >= 0 ? dragTick : tickAt(mouseX, current));
                int labelWidth = font.width(label);
                int labelX = Mth.clamp(mouseX - labelWidth / 2, getX() + 2, getX() + PROGRESS_WIDTH - labelWidth - 2);
                int labelY = barY - font.lineHeight - 3;
                graphics.fill(labelX - 2, labelY - 1, labelX + labelWidth + 2, labelY + font.lineHeight + 1, LABEL_BACK);
                graphics.text(font, label, labelX, labelY, TITLE_COLOR);
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            int barY = barY();
            return isActive() && mouseX >= getX() && mouseX < getX() + PROGRESS_WIDTH
                && mouseY >= barY - BAR_GRAB && mouseY < barY + BAR_HEIGHT + BAR_GRAB;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            SongEntry current = current();
            if (current != null) {
                dragSong = current.id();
                dragTick = tickAt(event.x(), current);
            }
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dx, double dy) {
            SongEntry current = current();
            if (dragTick >= 0 && current != null) {
                dragTick = tickAt(event.x(), current);
            }
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            if (dragTick >= 0) {
                ModNetworking.sendToServer(new SeekJukeboxPayload(pos, dragSong, dragTick));
                tick = dragTick;
                dragTick = -1;
            }
        }

        private int barY() {
            return getY() + PROGRESS_HEIGHT - BAR_HEIGHT;
        }

        private int tickAt(double mouseX, SongEntry current) {
            return Mth.clamp((int) ((mouseX - getX()) * current.length() / PROGRESS_WIDTH), 0, current.length() - 1);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private class SongList extends ObjectSelectionList<SongList.Entry> {

        SongList(Minecraft minecraft) {
            super(minecraft, JukeboxScreen.this.width, layout.getContentHeight(), layout.getHeaderHeight(), ROW_HEIGHT);
            filter("");
            select(playing);
        }

        void filter(String text) {
            query = text;
            Identifier selected = getSelected() == null ? null : getSelected().song.id();
            String lower = text.toLowerCase(Locale.ROOT);
            replaceEntries(songs.stream()
                .filter(song -> lower.isEmpty()
                    || song.title().toLowerCase(Locale.ROOT).contains(lower)
                    || song.author().toLowerCase(Locale.ROOT).contains(lower)
                    || song.id().toString().contains(lower))
                .sorted(sort.comparator(SongEntry::title, SongEntry::author, SongEntry::length))
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
                String duration = Songs.formatTime(song.length());
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
                    if (isPlaying && !paused) {
                        extractPauseIcon(graphics, buttonX, buttonY);
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

            private void extractPauseIcon(GuiGraphicsExtractor graphics, int buttonX, int buttonY) {
                int x = buttonX + (PLAY_SIZE - PAUSE_BAR_WIDTH * 2 - PAUSE_GAP) / 2;
                int y = buttonY + (PLAY_SIZE - PAUSE_ICON_HEIGHT) / 2;
                graphics.fill(x, y, x + PAUSE_BAR_WIDTH, y + PAUSE_ICON_HEIGHT, TITLE_COLOR);
                int secondX = x + PAUSE_BAR_WIDTH + PAUSE_GAP;
                graphics.fill(secondX, y, secondX + PAUSE_BAR_WIDTH, y + PAUSE_ICON_HEIGHT, TITLE_COLOR);
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
                    ModNetworking.sendToServer(new PauseJukeboxPayload(pos, !paused));
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
