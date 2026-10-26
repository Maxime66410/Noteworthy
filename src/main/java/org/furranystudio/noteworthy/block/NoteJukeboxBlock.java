/**
 * File: NoteJukeboxBlock.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-26
 */
package org.furranystudio.noteworthy.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.furranystudio.noteworthy.network.ModPackets;
import org.furranystudio.noteworthy.song.Song;

public class NoteJukeboxBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final String NOW_PLAYING_MARK = "♪ ";

    public NoteJukeboxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof NoteJukeboxBlockEntity jukebox) {
            if (player.isSecondaryUseActive()) {
                Song next = jukebox.playNext(serverPlayer.level().getServer());
                serverPlayer.sendOverlayMessage(next == null ? Component.translatable("noteworthy.jukebox.empty") : nowPlaying(next));
            } else {
                ModPackets.openJukebox(serverPlayer, jukebox);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static Component nowPlaying(Song song) {
        String text = song.author().isEmpty() ? song.displayTitle() : song.displayTitle() + " - " + song.author();
        return Component.literal(NOW_PLAYING_MARK + text).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NoteJukeboxBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.JUKEBOX.get()) {
            return null;
        }
        if (level.isClientSide()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<NoteJukeboxBlockEntity>) (tickLevel, pos, tickState, jukebox) ->
                jukebox.clientTick();
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<NoteJukeboxBlockEntity>) (tickLevel, pos, tickState, jukebox) ->
            jukebox.serverTick((ServerLevel) tickLevel, pos);
    }
}
