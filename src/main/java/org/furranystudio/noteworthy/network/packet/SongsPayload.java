/**
 * File: SongsPayload.java
 * Author: Maxime66410
 * Created: 2026-10-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.noteworthy.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.noteworthy.Noteworthy;

import java.util.ArrayList;
import java.util.List;

public record SongsPayload(List<SongEntry> songs) implements CustomPacketPayload {

    public static final Type<SongsPayload> TYPE = new Type<>(Noteworthy.id("songs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SongsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, SongEntry.STREAM_CODEC), SongsPayload::songs,
        SongsPayload::new);

    @Override
    public Type<SongsPayload> type() {
        return TYPE;
    }
}
