/**
 * File: StopJukeboxPayload.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.noteworthy.Noteworthy;

public record StopJukeboxPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<StopJukeboxPayload> TYPE = new Type<>(Noteworthy.id("stop_jukebox"));
    public static final StreamCodec<ByteBuf, StopJukeboxPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(StopJukeboxPayload::new, StopJukeboxPayload::pos);

    @Override
    public Type<StopJukeboxPayload> type() {
        return TYPE;
    }
}
