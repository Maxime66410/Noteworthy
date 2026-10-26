/**
 * File: SortJukeboxPayload.java
 * Author: Maxime66410
 * Created: 2026-10-26
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.song.SongSort;

public record SortJukeboxPayload(BlockPos pos, SongSort sort) implements CustomPacketPayload {

    public static final Type<SortJukeboxPayload> TYPE = new Type<>(Noteworthy.id("sort_jukebox"));
    public static final StreamCodec<ByteBuf, SortJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SortJukeboxPayload::pos,
        SongSort.STREAM_CODEC, SortJukeboxPayload::sort,
        SortJukeboxPayload::new);

    @Override
    public Type<SortJukeboxPayload> type() {
        return TYPE;
    }
}
