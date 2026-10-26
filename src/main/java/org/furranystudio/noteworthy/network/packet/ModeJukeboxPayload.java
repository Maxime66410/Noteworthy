/**
 * File: ModeJukeboxPayload.java
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
import org.furranystudio.noteworthy.song.PlayMode;

public record ModeJukeboxPayload(BlockPos pos, PlayMode mode) implements CustomPacketPayload {

    public static final Type<ModeJukeboxPayload> TYPE = new Type<>(Noteworthy.id("mode_jukebox"));
    public static final StreamCodec<ByteBuf, ModeJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ModeJukeboxPayload::pos,
        PlayMode.STREAM_CODEC, ModeJukeboxPayload::mode,
        ModeJukeboxPayload::new);

    @Override
    public Type<ModeJukeboxPayload> type() {
        return TYPE;
    }
}
