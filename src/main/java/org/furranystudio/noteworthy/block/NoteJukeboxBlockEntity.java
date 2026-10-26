/**
 * File: NoteJukeboxBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.noteworthy.network.ModPackets;
import org.furranystudio.noteworthy.song.PlayMode;
import org.furranystudio.noteworthy.song.Song;
import org.furranystudio.noteworthy.song.SongSort;
import org.furranystudio.noteworthy.song.Songs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoteJukeboxBlockEntity extends BlockEntity {

    private static final float VOLUME = 3.0F;
    private static final double PARTICLE_HEIGHT = 1.2;
    private static final double NOTE_COLORS = 24.0;
    private static final int RESTART_TICKS = 60;

    private Identifier song;
    private int tick;
    private boolean paused;
    private SongSort sort = SongSort.TITLE;
    private PlayMode mode = PlayMode.ONCE;
    private final List<Identifier> deck = new ArrayList<>();
    private int deckPos = -1;
    private String title = "";
    private String author = "";
    private int length;

    public NoteJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JUKEBOX.get(), pos, state);
    }

    public Identifier song() {
        return song;
    }

    public int tick() {
        return tick;
    }

    public boolean paused() {
        return paused;
    }

    public SongSort sort() {
        return sort;
    }

    public PlayMode mode() {
        return mode;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author;
    }

    public int length() {
        return length;
    }

    public void play(Identifier song) {
        if (mode == PlayMode.SHUFFLE && level instanceof ServerLevel serverLevel) {
            List<Song> songs = Songs.get(serverLevel.getServer());
            if (deck.isEmpty()) {
                newDeck(songs, song);
            } else {
                syncDeck(songs);
                chooseInDeck(song);
            }
        }
        start(song);
    }

    public Song playNext(MinecraftServer server) {
        List<Song> songs = sort.sort(Songs.get(server));
        if (songs.isEmpty()) {
            return null;
        }
        Song next = mode == PlayMode.SHUFFLE ? nextInDeck(songs) : nextInList(songs);
        start(next.id());
        return next;
    }

    public Song playPrevious(MinecraftServer server) {
        List<Song> songs = sort.sort(Songs.get(server));
        if (songs.isEmpty()) {
            return null;
        }
        Song previous = song != null && tick >= RESTART_TICKS ? byId(songs, song) : null;
        if (previous == null) {
            previous = mode == PlayMode.SHUFFLE ? previousInDeck(songs) : previousInList(songs);
        }
        start(previous.id());
        return previous;
    }

    private void start(Identifier song) {
        this.song = song;
        tick = 0;
        paused = false;
        changed();
    }

    private Song nextInList(List<Song> songs) {
        int current = -1;
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).id().equals(song)) {
                current = i;
            }
        }
        return songs.get((current + 1) % songs.size());
    }

    private Song previousInList(List<Song> songs) {
        int current = songs.size();
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).id().equals(song)) {
                current = i;
            }
        }
        return songs.get((current - 1 + songs.size()) % songs.size());
    }

    private Song previousInDeck(List<Song> songs) {
        if (!deck.isEmpty()) {
            syncDeck(songs);
        }
        if (deckPos < 0) {
            return nextInDeck(songs);
        }
        if (song != null && deckPos > 0) {
            deckPos--;
        }
        return byId(songs, deck.get(deckPos));
    }

    private Song nextInDeck(List<Song> songs) {
        if (deck.isEmpty()) {
            newDeck(songs, null);
        } else {
            syncDeck(songs);
        }
        if (deckPos + 1 >= deck.size()) {
            newDeck(songs, null);
        }
        deckPos++;
        return byId(songs, deck.get(deckPos));
    }

    private static Song byId(List<Song> songs, Identifier id) {
        return songs.stream().filter(candidate -> candidate.id().equals(id)).findFirst().orElse(null);
    }

    private void newDeck(List<Song> songs, Identifier first) {
        deck.clear();
        songs.forEach(candidate -> deck.add(candidate.id()));
        for (int i = deck.size() - 1; i > 0; i--) {
            Collections.swap(deck, i, level.getRandom().nextInt(i + 1));
        }
        if (first != null) {
            deck.remove(first);
            deck.add(0, first);
            deckPos = 0;
            return;
        }
        deckPos = -1;
        if (deck.size() > 1 && deck.getFirst().equals(song)) {
            Identifier last = deck.removeFirst();
            deck.add(1 + level.getRandom().nextInt(deck.size()), last);
        }
    }

    private void syncDeck(List<Song> songs) {
        Set<Identifier> ids = songs.stream().map(Song::id).collect(Collectors.toSet());
        for (int i = deck.size() - 1; i >= 0; i--) {
            if (!ids.contains(deck.get(i))) {
                deck.remove(i);
                if (i <= deckPos) {
                    deckPos--;
                }
            }
        }
        for (Song candidate : songs) {
            if (!deck.contains(candidate.id())) {
                deck.add(deckPos + 1 + level.getRandom().nextInt(deck.size() - deckPos), candidate.id());
            }
        }
    }

    private void chooseInDeck(Identifier song) {
        if (deckPos >= 0 && deck.get(deckPos).equals(song)) {
            return;
        }
        for (int i = deckPos + 1; i < deck.size(); i++) {
            if (deck.get(i).equals(song)) {
                deck.remove(i);
                break;
            }
        }
        deckPos++;
        deck.add(deckPos, song);
    }

    public void seek(int tick) {
        Song playing = song != null && level instanceof ServerLevel serverLevel ? Songs.find(serverLevel.getServer(), song) : null;
        if (playing == null) {
            return;
        }
        this.tick = Mth.clamp(tick, 0, playing.length() - 1);
        changed();
    }

    public void setPaused(boolean paused) {
        if (song == null || this.paused == paused) {
            return;
        }
        this.paused = paused;
        changed();
    }

    public void setSort(SongSort sort) {
        if (this.sort == sort) {
            return;
        }
        this.sort = sort;
        changed();
    }

    public void setMode(PlayMode mode) {
        if (this.mode == mode) {
            return;
        }
        this.mode = mode;
        deck.clear();
        deckPos = -1;
        if (mode == PlayMode.SHUFFLE && song != null && level instanceof ServerLevel serverLevel) {
            newDeck(Songs.get(serverLevel.getServer()), song);
        }
        changed();
    }

    public void stop() {
        song = null;
        tick = 0;
        paused = false;
        changed();
    }

    private void changed() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            ModPackets.sendState(serverLevel, this);
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    void clientTick() {
        if (song != null && !paused && tick < length) {
            tick++;
        }
    }

    void serverTick(ServerLevel level, BlockPos pos) {
        if (song == null) {
            return;
        }
        Song playing = Songs.find(level.getServer(), song);
        if (playing == null) {
            stop();
            return;
        }
        if (tick >= playing.length()) {
            finish(level.getServer());
            return;
        }
        if (paused) {
            return;
        }
        for (Song.Part part : playing.parts()) {
            int[] notes = part.notes().get(tick);
            if (notes != null) {
                playNotes(level, pos, part, notes);
            }
        }
        tick++;
        setChanged();
    }

    private void finish(MinecraftServer server) {
        switch (mode) {
            case ONCE -> stop();
            case REPEAT -> {
                tick = 0;
                changed();
            }
            case LIST, SHUFFLE -> {
                if (playNext(server) == null) {
                    stop();
                }
            }
        }
    }

    private static void playNotes(ServerLevel level, BlockPos pos, Song.Part part, int[] notes) {
        boolean tunable = part.instrument().isTunable();
        for (int note : notes) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, part.instrument().getSoundEvent().value(),
                SoundSource.RECORDS, VOLUME, tunable ? NoteBlock.getPitchFromNote(note) : 1.0F);
            if (tunable) {
                level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + PARTICLE_HEIGHT, pos.getZ() + 0.5,
                    0, note / NOTE_COLORS, 0.0, 0.0, 1.0);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Sort", sort.getName());
        output.putString("Mode", mode.getName());
        if (!deck.isEmpty()) {
            output.store("Deck", Identifier.CODEC.listOf(), List.copyOf(deck));
            output.putInt("DeckPos", deckPos);
        }
        if (song != null) {
            output.putString("Song", song.toString());
            output.putInt("Tick", tick);
            output.putBoolean("Paused", paused);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        song = input.getString("Song").map(Identifier::tryParse).orElse(null);
        tick = input.getIntOr("Tick", 0);
        paused = input.getBooleanOr("Paused", false);
        sort = SongSort.byName(input.getStringOr("Sort", SongSort.TITLE.getName()));
        mode = PlayMode.byName(input.getStringOr("Mode", PlayMode.ONCE.getName()));
        deck.clear();
        input.read("Deck", Identifier.CODEC.listOf()).ifPresent(deck::addAll);
        deckPos = Mth.clamp(input.getIntOr("DeckPos", -1), -1, deck.size() - 1);
        title = input.getStringOr("Title", "");
        author = input.getStringOr("Author", "");
        length = input.getIntOr("Length", 0);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Title, author and length only travel to clients, the server reads them from the song list
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveCustomOnly(registries);
        Song playing = song != null && level instanceof ServerLevel serverLevel ? Songs.find(serverLevel.getServer(), song) : null;
        if (playing != null) {
            tag.putString("Title", playing.displayTitle());
            tag.putString("Author", playing.author());
            tag.putInt("Length", playing.length());
        }
        return tag;
    }
}
