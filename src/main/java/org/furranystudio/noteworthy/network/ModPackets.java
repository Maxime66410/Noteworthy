/**
 * File: ModPackets.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-27
 */
package org.furranystudio.noteworthy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.noteworthy.block.NoteJukeboxBlockEntity;
import org.furranystudio.noteworthy.network.packet.JukeboxNotesPayload;
import org.furranystudio.noteworthy.network.packet.JukeboxStatePayload;
import org.furranystudio.noteworthy.network.packet.ModeJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.OpenJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.PauseJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.PlayJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.RangeJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.SeekJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.SkipJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.SongEntry;
import org.furranystudio.noteworthy.network.packet.SongsPayload;
import org.furranystudio.noteworthy.network.packet.SortJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.StopJukeboxPayload;
import org.furranystudio.noteworthy.network.packet.VolumeJukeboxPayload;
import org.furranystudio.noteworthy.song.Songs;

import java.util.List;
import java.util.Optional;

public final class ModPackets {

    private static final double STATE_RANGE = 64.0;

    private ModPackets() {
    }

    public static void init() {
        ModNetworking.clientbound(OpenJukeboxPayload.TYPE, OpenJukeboxPayload.STREAM_CODEC);
        ModNetworking.clientbound(JukeboxStatePayload.TYPE, JukeboxStatePayload.STREAM_CODEC);
        ModNetworking.clientbound(JukeboxNotesPayload.TYPE, JukeboxNotesPayload.STREAM_CODEC);
        ModNetworking.clientbound(SongsPayload.TYPE, SongsPayload.STREAM_CODEC);
        ModNetworking.serverbound(PlayJukeboxPayload.TYPE, PlayJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null && Songs.find(player.level().getServer(), payload.song()) != null) {
                jukebox.play(payload.song());
            }
        });
        ModNetworking.serverbound(StopJukeboxPayload.TYPE, StopJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                jukebox.stop();
            }
        });
        ModNetworking.serverbound(PauseJukeboxPayload.TYPE, PauseJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                jukebox.setPaused(payload.paused());
            }
        });
        ModNetworking.serverbound(SkipJukeboxPayload.TYPE, SkipJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                if (payload.forward()) {
                    jukebox.playNext(player.level().getServer());
                } else {
                    jukebox.playPrevious(player.level().getServer());
                }
            }
        });
        ModNetworking.serverbound(ModeJukeboxPayload.TYPE, ModeJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                jukebox.setMode(payload.mode());
            }
        });
        ModNetworking.serverbound(SortJukeboxPayload.TYPE, SortJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                jukebox.setSort(payload.sort());
            }
        });
        ModNetworking.serverbound(RangeJukeboxPayload.TYPE, RangeJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                jukebox.setRange(payload.range());
            }
        });
        ModNetworking.serverbound(VolumeJukeboxPayload.TYPE, VolumeJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null) {
                jukebox.setVolume(payload.volume());
            }
        });
        ModNetworking.serverbound(SeekJukeboxPayload.TYPE, SeekJukeboxPayload.STREAM_CODEC, (payload, player) -> {
            NoteJukeboxBlockEntity jukebox = reachableJukebox(player, payload.pos());
            if (jukebox != null && payload.song().equals(jukebox.song())) {
                jukebox.seek(payload.tick());
            }
        });
    }

    public static void openJukebox(ServerPlayer player, NoteJukeboxBlockEntity jukebox) {
        List<SongEntry> songs = Songs.get(player.level().getServer()).stream().map(SongEntry::of).toList();
        ModNetworking.sendToPlayer(player, new OpenJukeboxPayload(jukebox.getBlockPos(), songs, state(jukebox)));
    }

    public static void sendSongs(MinecraftServer server) {
        SongsPayload songs = new SongsPayload(Songs.get(server).stream().map(SongEntry::of).toList());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ModNetworking.sendToPlayer(player, songs);
        }
    }

    public static void sendState(ServerLevel level, NoteJukeboxBlockEntity jukebox) {
        JukeboxStatePayload state = state(jukebox);
        Vec3 center = Vec3.atCenterOf(jukebox.getBlockPos());
        for (ServerPlayer player : level.players()) {
            if (player.position().closerThan(center, STATE_RANGE)) {
                ModNetworking.sendToPlayer(player, state);
            }
        }
    }

    public static void sendNotes(ServerLevel level, JukeboxNotesPayload notes) {
        Vec3 center = Vec3.atCenterOf(notes.pos());
        for (ServerPlayer player : level.players()) {
            if (player.position().closerThan(center, notes.range())) {
                ModNetworking.sendToPlayer(player, notes);
            }
        }
    }

    private static JukeboxStatePayload state(NoteJukeboxBlockEntity jukebox) {
        return new JukeboxStatePayload(jukebox.getBlockPos(), Optional.ofNullable(jukebox.song()), jukebox.tick(), jukebox.paused(), jukebox.sort(), jukebox.mode(),
            jukebox.range(), jukebox.volume());
    }

    private static NoteJukeboxBlockEntity reachableJukebox(ServerPlayer player, BlockPos pos) {
        double range = player.blockInteractionRange() + 1.0;
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > Mth.square(range)) {
            return null;
        }
        return player.level().getBlockEntity(pos) instanceof NoteJukeboxBlockEntity jukebox ? jukebox : null;
    }
}
