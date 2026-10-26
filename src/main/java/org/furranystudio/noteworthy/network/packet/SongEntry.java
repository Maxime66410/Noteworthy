/**
 * File: SongEntry.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.furranystudio.noteworthy.song.Song;

import java.util.List;

public record SongEntry(Identifier id, String title, String author, int length, List<String> instruments, int parts) {

    public static final StreamCodec<ByteBuf, SongEntry> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, SongEntry::id,
        ByteBufCodecs.STRING_UTF8, SongEntry::title,
        ByteBufCodecs.STRING_UTF8, SongEntry::author,
        ByteBufCodecs.VAR_INT, SongEntry::length,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SongEntry::instruments,
        ByteBufCodecs.VAR_INT, SongEntry::parts,
        SongEntry::new);

    public static SongEntry of(Song song) {
        List<String> instruments = song.parts().stream().map(part -> part.instrument().getSerializedName()).distinct().toList();
        return new SongEntry(song.id(), song.displayTitle(), song.author(), song.length(), instruments, song.parts().size());
    }
}
