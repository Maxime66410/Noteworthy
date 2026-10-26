/**
 * File: SkipJukeboxPayload.java
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

public record SkipJukeboxPayload(BlockPos pos, boolean forward) implements CustomPacketPayload {

    public static final Type<SkipJukeboxPayload> TYPE = new Type<>(Noteworthy.id("skip_jukebox"));
    public static final StreamCodec<ByteBuf, SkipJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SkipJukeboxPayload::pos,
        ByteBufCodecs.BOOL, SkipJukeboxPayload::forward,
        SkipJukeboxPayload::new);

    @Override
    public Type<SkipJukeboxPayload> type() {
        return TYPE;
    }
}
