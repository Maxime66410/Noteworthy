/**
 * File: VolumeJukeboxPayload.java
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

public record VolumeJukeboxPayload(BlockPos pos, int volume) implements CustomPacketPayload {

    public static final Type<VolumeJukeboxPayload> TYPE = new Type<>(Noteworthy.id("volume_jukebox"));
    public static final StreamCodec<ByteBuf, VolumeJukeboxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, VolumeJukeboxPayload::pos,
        ByteBufCodecs.VAR_INT, VolumeJukeboxPayload::volume,
        VolumeJukeboxPayload::new);

    @Override
    public Type<VolumeJukeboxPayload> type() {
        return TYPE;
    }
}
