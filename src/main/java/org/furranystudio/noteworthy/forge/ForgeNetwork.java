/**
 * File: ForgeNetwork.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.forge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.payload.PayloadFlow;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.network.ModNetworking;

final class ForgeNetwork {

    private static final int PROTOCOL_VERSION = 1;

    private ForgeNetwork() {
    }

    static void register() {
        PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> flow = ChannelBuilder.named(Noteworthy.id("main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .payloadChannel()
            .play()
            .clientbound();
        for (ModNetworking.Clientbound<?> packet : ModNetworking.clientboundPackets()) {
            flow = addClientbound(flow, packet);
        }
        flow = flow.serverbound();
        for (ModNetworking.Serverbound<?> packet : ModNetworking.serverboundPackets()) {
            flow = addServerbound(flow, packet);
        }
        Channel<CustomPacketPayload> channel = flow.build();

        ModNetworking.init(new ModNetworking.Sender() {
            @Override
            public void toPlayer(ServerPlayer player, CustomPacketPayload payload) {
                channel.send(payload, PacketDistributor.PLAYER.with(player));
            }

            @Override
            public void toServer(CustomPacketPayload payload) {
                channel.send(payload, PacketDistributor.SERVER.noArg());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> addClientbound(
            PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> flow, ModNetworking.Clientbound<T> packet) {
        return flow.addMain(packet.type(), (StreamCodec<RegistryFriendlyByteBuf, T>) packet.codec(),
            (payload, context) -> ModNetworking.handleOnClient(payload));
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> addServerbound(
            PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> flow, ModNetworking.Serverbound<T> packet) {
        return flow.addMain(packet.type(), (StreamCodec<RegistryFriendlyByteBuf, T>) packet.codec(),
            (payload, context) -> packet.handler().accept(payload, context.getSender()));
    }
}
