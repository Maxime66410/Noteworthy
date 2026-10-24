/**
 * File: OpenJukeboxPayload.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.noteworthy.Noteworthy;

import java.util.ArrayList;
import java.util.List;

public record OpenJukeboxPayload(BlockPos pos, List<SongEntry> songs, JukeboxStatePayload state) implements CustomPacketPayload {

    public static final Type<OpenJukeboxPayload> TYPE = new Type<>(Noteworthy.id("open_jukebox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OpenJukeboxPayload::pos,
        ByteBufCodecs.collection(ArrayList::new, SongEntry.STREAM_CODEC), OpenJukeboxPayload::songs,
        JukeboxStatePayload.STREAM_CODEC, OpenJukeboxPayload::state,
        OpenJukeboxPayload::new);

    @Override
    public Type<OpenJukeboxPayload> type() {
        return TYPE;
    }
}
