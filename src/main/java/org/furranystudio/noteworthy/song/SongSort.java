/**
 * File: SongSort.java
 * Author: Maxime66410
 * Created: 2026-10-26
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.song;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public enum SongSort {
    TITLE("title"),
    AUTHOR("author"),
    DURATION("duration");

    public static final StreamCodec<ByteBuf, SongSort> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(SongSort::byId, Enum::ordinal);

    private final String name;

    SongSort(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Component displayName() {
        return Component.translatable("noteworthy.sort." + name);
    }

    public SongSort next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public List<Song> sort(List<Song> songs) {
        return songs.stream().sorted(comparator(Song::displayTitle, Song::author, Song::length)).toList();
    }

    public <T> Comparator<T> comparator(Function<T, String> title, Function<T, String> author, ToIntFunction<T> length) {
        Comparator<T> byTitle = Comparator.comparing(title, String.CASE_INSENSITIVE_ORDER);
        return switch (this) {
            case TITLE -> byTitle;
            case AUTHOR -> Comparator.<T, Boolean>comparing(song -> author.apply(song).isEmpty())
                .thenComparing(author, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(byTitle);
            case DURATION -> Comparator.comparingInt(length).thenComparing(byTitle);
        };
    }

    public static SongSort byName(String name) {
        for (SongSort sort : values()) {
            if (sort.name.equals(name)) {
                return sort;
            }
        }
        return TITLE;
    }

    private static SongSort byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : TITLE;
    }
}
