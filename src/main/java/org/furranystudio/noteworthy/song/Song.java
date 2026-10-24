/**
 * File: Song.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.song;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.List;
import java.util.Map;

public record Song(Identifier id, String title, String author, int length, List<Part> parts) {

    public record Part(NoteBlockInstrument instrument, Map<Integer, int[]> notes) {
    }

    public String displayTitle() {
        if (!title.isEmpty()) {
            return title;
        }
        String path = id.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
