/**
 * File: JukeboxStatePayload.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.song.SongSort;

import java.util.Optional;

public record JukeboxStatePayload(BlockPos pos, Optional<Identifier> song, int tick, boolean paused, SongSort sort) implements CustomPacketPayload {

    public static final Type<JukeboxStatePayload> TYPE = new Type<>(Noteworthy.id("jukebox_state"));
    public static final StreamCodec<ByteBuf, JukeboxStatePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, JukeboxStatePayload::pos,
        ByteBufCodecs.optional(Identifier.STREAM_CODEC), JukeboxStatePayload::song,
        ByteBufCodecs.VAR_INT, JukeboxStatePayload::tick,
        ByteBufCodecs.BOOL, JukeboxStatePayload::paused,
        SongSort.STREAM_CODEC, JukeboxStatePayload::sort,
        JukeboxStatePayload::new);

    @Override
    public Type<JukeboxStatePayload> type() {
        return TYPE;
    }
}
