/**
 * File: PlayMode.java
 * Author: Maxime66410
 * Created: 2026-10-26
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.song;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum PlayMode {
    ONCE("once"),
    REPEAT("repeat"),
    LIST("list"),
    SHUFFLE("shuffle");

    public static final StreamCodec<ByteBuf, PlayMode> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(PlayMode::byId, Enum::ordinal);

    private final String name;

    PlayMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Component displayName() {
        return Component.translatable("noteworthy.mode." + name);
    }

    public PlayMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static PlayMode byName(String name) {
        for (PlayMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return ONCE;
    }

    private static PlayMode byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : ONCE;
    }
}
