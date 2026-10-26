/**
 * File: JukeboxSettingsScreen.java
 * Author: Maxime66410
 * Created: 2026-10-26
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.furranystudio.noteworthy.block.NoteJukeboxBlockEntity;
import org.furranystudio.noteworthy.network.ModNetworking;
import org.furranystudio.noteworthy.network.packet.RangeJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.VolumeJukeboxPayload;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

public class JukeboxSettingsScreen extends Screen {

    private static final int SLIDER_WIDTH = 200;
    private static final int VOLUME_STEP = 5;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final JukeboxScreen parent;
    private SettingSlider rangeSlider;
    private SettingSlider volumeSlider;

    public JukeboxSettingsScreen(JukeboxScreen parent) {
        super(Component.translatable("noteworthy.settings.title"));
        this.parent = parent;
    }

    public JukeboxScreen parent() {
        return parent;
    }

    @Override
    protected void init() {
        layout.addToHeader(new StringWidget(title, font));
        LinearLayout contents = layout.addToContents(LinearLayout.vertical().spacing(8));
        rangeSlider = contents.addChild(new SettingSlider(NoteJukeboxBlockEntity.MIN_RANGE, NoteJukeboxBlockEntity.MAX_RANGE, 1,
            parent.range(), range -> Component.translatable("noteworthy.settings.range", range),
            range -> ModNetworking.sendToServer(new RangeJukeboxPayload(parent.pos(), range))));
        volumeSlider = contents.addChild(new SettingSlider(0, NoteJukeboxBlockEntity.MAX_VOLUME, VOLUME_STEP,
            parent.volume(), volume -> Component.translatable("noteworthy.settings.volume", volume),
            volume -> ModNetworking.sendToServer(new VolumeJukeboxPayload(parent.pos(), volume))));
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        parent.tick();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    public void refresh() {
        if (rangeSlider != null) {
            rangeSlider.show(parent.range());
            volumeSlider.show(parent.volume());
        }
    }

    private static class SettingSlider extends AbstractSliderButton {

        private final int min;
        private final int max;
        private final int step;
        private final IntFunction<Component> label;
        private final IntConsumer send;
        private int current;
        private boolean held;

        SettingSlider(int min, int max, int step, int current, IntFunction<Component> label, IntConsumer send) {
            super(0, 0, SLIDER_WIDTH, DEFAULT_HEIGHT, label.apply(current), (current - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.step = step;
            this.label = label;
            this.send = send;
            this.current = current;
        }

        void show(int current) {
            if (held) {
                return;
            }
            this.current = current;
            value = (current - min) / (double) (max - min);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(label.apply(current));
        }

        @Override
        protected void applyValue() {
            int stepped = min + (int) Math.round(value * (max - min) / step) * step;
            if (stepped != current) {
                current = stepped;
                send.accept(stepped);
            }
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            held = true;
            super.onClick(event, doubleClick);
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            super.onRelease(event);
            held = false;
            show(current);
        }
    }
}
