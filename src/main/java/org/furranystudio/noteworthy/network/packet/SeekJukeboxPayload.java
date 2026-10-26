/**
 * File: SeekJukeboxPayload.java
 * Author: Maxime66410
 * Created: 2026-10-26
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

public record SeekJukeboxPayload(BlockPos pos, Identifier song, int tick) implements CustomPacketPayload {

    public static final Type<SeekJukeboxPayload> TYPE = new Type<>(Noteworthy.id("seek_jukebox"));
    public static final StreamCodec<ByteBuf, SeekJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SeekJukeboxPayload::pos,
        Identifier.STREAM_CODEC, SeekJukeboxPayload::song,
        ByteBufCodecs.VAR_INT, SeekJukeboxPayload::tick,
        SeekJukeboxPayload::new);

    @Override
    public Type<SeekJukeboxPayload> type() {
        return TYPE;
    }
}
