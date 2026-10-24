/**
 * File: SongEntry.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.furranystudio.noteworthy.song.Song;

public record SongEntry(Identifier id, String title, String author, int length) {

    public static final StreamCodec<ByteBuf, SongEntry> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, SongEntry::id,
        ByteBufCodecs.STRING_UTF8, SongEntry::title,
        ByteBufCodecs.STRING_UTF8, SongEntry::author,
        ByteBufCodecs.VAR_INT, SongEntry::length,
        SongEntry::new);

    public static SongEntry of(Song song) {
        return new SongEntry(song.id(), song.displayTitle(), song.author(), song.length());
    }
}
