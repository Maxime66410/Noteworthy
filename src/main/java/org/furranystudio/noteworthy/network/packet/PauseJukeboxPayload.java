/**
 * File: PauseJukeboxPayload.java
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

public record PauseJukeboxPayload(BlockPos pos, boolean paused) implements CustomPacketPayload {

    public static final Type<PauseJukeboxPayload> TYPE = new Type<>(Noteworthy.id("pause_jukebox"));
    public static final StreamCodec<ByteBuf, PauseJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, PauseJukeboxPayload::pos,
        ByteBufCodecs.BOOL, PauseJukeboxPayload::paused,
        PauseJukeboxPayload::new);

    @Override
    public Type<PauseJukeboxPayload> type() {
        return TYPE;
    }
}
