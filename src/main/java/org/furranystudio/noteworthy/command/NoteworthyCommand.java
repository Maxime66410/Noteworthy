/**
 * File: NoteworthyCommand.java
 * Author: Maxime66410
 * Created: 2026-10-25
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.furranystudio.noteworthy.block.NoteJukeboxBlockEntity;
import org.furranystudio.noteworthy.song.Song;
import org.furranystudio.noteworthy.song.Songs;

import java.util.List;

public final class NoteworthyCommand {

    private static final double TARGET_RANGE = 20.0;
    private static final SuggestionProvider<CommandSourceStack> SONGS = (context, builder) ->
        SharedSuggestionProvider.suggestResource(Songs.get(context.getSource().getServer()).stream().map(Song::id), builder);
    private static final SimpleCommandExceptionType NO_TARGET =
        new SimpleCommandExceptionType(Component.translatable("noteworthy.command.no_target"));
    private static final DynamicCommandExceptionType NOT_JUKEBOX = new DynamicCommandExceptionType(pos ->
        Component.translatable("noteworthy.command.not_jukebox", ((BlockPos) pos).getX(), ((BlockPos) pos).getY(), ((BlockPos) pos).getZ()));
    private static final SimpleCommandExceptionType NOT_PLAYING =
        new SimpleCommandExceptionType(Component.translatable("noteworthy.command.not_playing"));
    private static final SimpleCommandExceptionType ALREADY_PAUSED =
        new SimpleCommandExceptionType(Component.translatable("noteworthy.command.already_paused"));
    private static final SimpleCommandExceptionType NOT_PAUSED =
        new SimpleCommandExceptionType(Component.translatable("noteworthy.command.not_paused"));
    private static final DynamicCommandExceptionType UNKNOWN_SONG = new DynamicCommandExceptionType(id ->
        Component.translatable("noteworthy.command.unknown_song", String.valueOf(id)));

    private NoteworthyCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("noteworthy")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list")
                    .executes(NoteworthyCommand::runList))
                .then(Commands.literal("play")
                    .then(Commands.argument("song", IdentifierArgument.id())
                        .suggests(SONGS)
                        .executes(context -> runPlay(context, targetedJukebox(context)))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes(context -> runPlay(context, jukeboxAt(context))))))
                .then(Commands.literal("stop")
                    .executes(context -> runStop(context, targetedJukebox(context)))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> runStop(context, jukeboxAt(context)))))
                .then(Commands.literal("pause")
                    .executes(context -> runPause(context, targetedJukebox(context), true))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> runPause(context, jukeboxAt(context), true))))
                .then(Commands.literal("resume")
                    .executes(context -> runPause(context, targetedJukebox(context), false))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> runPause(context, jukeboxAt(context), false))))
        );
    }

    private static int runList(CommandContext<CommandSourceStack> context) {
        List<Song> songs = Songs.get(context.getSource().getServer());
        MutableComponent message = Component.translatable("noteworthy.command.list", songs.size());
        for (Song song : songs) {
            message.append(Component.translatable("noteworthy.command.list.entry", song.id().toString(), song.displayTitle(),
                Songs.formatTime(song.length())));
        }
        context.getSource().sendSuccess(() -> message, false);
        return songs.size();
    }

    private static int runPlay(CommandContext<CommandSourceStack> context, NoteJukeboxBlockEntity jukebox) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "song");
        Song song = Songs.find(context.getSource().getServer(), id);
        if (song == null) {
            throw UNKNOWN_SONG.create(id);
        }
        jukebox.play(song.id());
        BlockPos pos = jukebox.getBlockPos();
        context.getSource().sendSuccess(() -> Component.translatable("noteworthy.command.play", song.displayTitle(),
            pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static int runStop(CommandContext<CommandSourceStack> context, NoteJukeboxBlockEntity jukebox) {
        jukebox.stop();
        BlockPos pos = jukebox.getBlockPos();
        context.getSource().sendSuccess(() -> Component.translatable("noteworthy.command.stop", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static int runPause(CommandContext<CommandSourceStack> context, NoteJukeboxBlockEntity jukebox, boolean paused) throws CommandSyntaxException {
        if (jukebox.song() == null) {
            throw NOT_PLAYING.create();
        }
        if (jukebox.paused() == paused) {
            throw (paused ? ALREADY_PAUSED : NOT_PAUSED).create();
        }
        jukebox.setPaused(paused);
        BlockPos pos = jukebox.getBlockPos();
        context.getSource().sendSuccess(() -> Component.translatable(paused ? "noteworthy.command.pause" : "noteworthy.command.resume",
            pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static NoteJukeboxBlockEntity targetedJukebox(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (player.pick(TARGET_RANGE, 1.0F, false) instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && player.level().getBlockEntity(hit.getBlockPos()) instanceof NoteJukeboxBlockEntity jukebox) {
            return jukebox;
        }
        throw NO_TARGET.create();
    }

    private static NoteJukeboxBlockEntity jukeboxAt(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        if (context.getSource().getLevel().getBlockEntity(pos) instanceof NoteJukeboxBlockEntity jukebox) {
            return jukebox;
        }
        throw NOT_JUKEBOX.create(pos);
    }
}
