/**
 * File: RangeJukeboxPayload.java
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
import org.furranystudio.noteworthy.Noteworthy;

public record RangeJukeboxPayload(BlockPos pos, int range) implements CustomPacketPayload {

    public static final Type<RangeJukeboxPayload> TYPE = new Type<>(Noteworthy.id("range_jukebox"));
    public static final StreamCodec<ByteBuf, RangeJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RangeJukeboxPayload::pos,
        ByteBufCodecs.VAR_INT, RangeJukeboxPayload::range,
        RangeJukeboxPayload::new);

    @Override
    public Type<RangeJukeboxPayload> type() {
        return TYPE;
    }
}
