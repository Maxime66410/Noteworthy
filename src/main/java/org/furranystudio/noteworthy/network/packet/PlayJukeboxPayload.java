/**
 * File: PlayJukeboxPayload.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.furranystudio.noteworthy.Noteworthy;

public record PlayJukeboxPayload(BlockPos pos, Identifier song) implements CustomPacketPayload {

    public static final Type<PlayJukeboxPayload> TYPE = new Type<>(Noteworthy.id("play_jukebox"));
    public static final StreamCodec<ByteBuf, PlayJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, PlayJukeboxPayload::pos,
        Identifier.STREAM_CODEC, PlayJukeboxPayload::song,
        PlayJukeboxPayload::new);

    @Override
    public Type<PlayJukeboxPayload> type() {
        return TYPE;
    }
}
