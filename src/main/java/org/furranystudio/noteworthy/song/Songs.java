/**
 * File: Songs.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.song;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.furranystudio.noteworthy.Noteworthy;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Songs {

    private static final String DIRECTORY = "noteworthy/songs";
    private static final String EXTENSION = ".json";
    private static final List<String> NAMES = List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static final List<String> FLATS = List.of("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B");
    private static final int LOWEST = NAMES.indexOf("F#") + 3 * 12;
    private static final int RANGE = 24;
    private static final String REST = "-";

    // Reloaded when the resource manager changes, so /reload picks up edited songs
    private static ResourceManager loadedFrom;
    private static List<Song> songs = List.of();

    private Songs() {
    }

    public static List<Song> get(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            songs = load(manager);
        }
        return songs;
    }

    public static Song find(MinecraftServer server, Identifier id) {
        return get(server).stream().filter(song -> song.id().equals(id)).findFirst().orElse(null);
    }

    private static List<Song> load(ResourceManager manager) {
        List<Song> result = new ArrayList<>();
        Map<Identifier, Resource> files = manager.listResources(DIRECTORY, id -> id.getPath().endsWith(EXTENSION));
        files.forEach((file, resource) -> {
            String path = file.getPath();
            Identifier id = Identifier.fromNamespaceAndPath(file.getNamespace(),
                path.substring(DIRECTORY.length() + 1, path.length() - EXTENSION.length()));
            try (Reader reader = resource.openAsReader()) {
                result.add(parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception e) {
                Noteworthy.LOGGER.error("[Noteworthy] Couldn't read song {}", file, e);
            }
        });
        result.sort(Comparator.comparing(Song::displayTitle, String.CASE_INSENSITIVE_ORDER));
        Noteworthy.LOGGER.info("[Noteworthy] Loaded {} songs.", result.size());
        return List.copyOf(result);
    }

    private static Song parse(Identifier id, JsonObject root) {
        int tempo = root.get("tempo").getAsInt();
        if (tempo < 1) {
            throw new IllegalArgumentException("Tempo must be at least 1");
        }
        List<Song.Part> parts = new ArrayList<>();
        int length = 0;
        for (JsonElement element : root.getAsJsonArray("parts")) {
            JsonObject part = element.getAsJsonObject();
            String name = part.get("instrument").getAsString();
            NoteBlockInstrument instrument = Arrays.stream(NoteBlockInstrument.values())
                .filter(value -> value.getSerializedName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown instrument " + name));
            Map<Integer, int[]> notes = new HashMap<>();
            int step = 0;
            for (JsonElement token : part.getAsJsonArray("notes")) {
                String[] split = token.getAsString().split(":");
                int steps = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                if (steps < 1) {
                    throw new IllegalArgumentException("Length of " + token.getAsString() + " must be at least 1");
                }
                if (!split[0].equals(REST)) {
                    notes.put(step * tempo, Arrays.stream(split[0].split("\\+")).mapToInt(Songs::note).toArray());
                }
                step += steps;
            }
            length = Math.max(length, step * tempo);
            parts.add(new Song.Part(instrument, notes));
        }
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Song has no parts");
        }
        return new Song(id, optionalText(root, "title"), optionalText(root, "author"), length, List.copyOf(parts));
    }

    private static String optionalText(JsonObject root, String key) {
        return root.has(key) ? root.get(key).getAsString().trim() : "";
    }

    private static int note(String name) {
        if (Character.isDigit(name.charAt(0))) {
            return check(Integer.parseInt(name), name);
        }
        int split = name.length() > 2 && (name.charAt(1) == '#' || name.charAt(1) == 'b') ? 2 : 1;
        String letter = name.substring(0, split);
        int index = NAMES.contains(letter) ? NAMES.indexOf(letter) : FLATS.indexOf(letter);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown note " + name);
        }
        return check(index + Integer.parseInt(name.substring(split)) * 12 - LOWEST, name);
    }

    private static int check(int note, String name) {
        if (note < 0 || note > RANGE) {
            throw new IllegalArgumentException("Note " + name + " is outside F#3 to F#5");
        }
        return note;
    }
}
