/**
 * File: JukeboxNotesPayload.java
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

import java.util.ArrayList;
import java.util.List;

public record JukeboxNotesPayload(BlockPos pos, int range, int volume, List<Note> notes) implements CustomPacketPayload {

    public record Note(Identifier sound, float pitch) {

        public static final StreamCodec<ByteBuf, Note> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, Note::sound,
            ByteBufCodecs.FLOAT, Note::pitch,
            Note::new);
    }

    public static final Type<JukeboxNotesPayload> TYPE = new Type<>(Noteworthy.id("jukebox_notes"));
    public static final StreamCodec<ByteBuf, JukeboxNotesPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, JukeboxNotesPayload::pos,
        ByteBufCodecs.VAR_INT, JukeboxNotesPayload::range,
        ByteBufCodecs.VAR_INT, JukeboxNotesPayload::volume,
        ByteBufCodecs.collection(ArrayList::new, Note.STREAM_CODEC), JukeboxNotesPayload::notes,
        JukeboxNotesPayload::new);

    @Override
    public Type<JukeboxNotesPayload> type() {
        return TYPE;
    }
}
