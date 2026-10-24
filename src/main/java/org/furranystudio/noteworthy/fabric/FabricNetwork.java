/**
 * File: FabricNetwork.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.noteworthy.network.ModNetworking;

final class FabricNetwork {

    private FabricNetwork() {
    }

    static void register() {
        for (ModNetworking.Clientbound<?> packet : ModNetworking.clientboundPackets()) {
            registerClientboundType(packet);
        }
        for (ModNetworking.Serverbound<?> packet : ModNetworking.serverboundPackets()) {
            registerServerbound(packet);
        }

        ModNetworking.init(new ModNetworking.Sender() {
            @Override
            public void toPlayer(ServerPlayer player, CustomPacketPayload payload) {
                ServerPlayNetworking.send(player, payload);
            }

            @Override
            public void toServer(CustomPacketPayload payload) {
                ClientPlayNetworking.send(payload);
            }
        });
    }

    static void registerClient() {
        for (ModNetworking.Clientbound<?> packet : ModNetworking.clientboundPackets()) {
            registerClientReceiver(packet);
        }
    }

    private static <T extends CustomPacketPayload> void registerClientboundType(ModNetworking.Clientbound<T> packet) {
        PayloadTypeRegistry.clientboundPlay().register(packet.type(), packet.codec());
    }

    private static <T extends CustomPacketPayload> void registerServerbound(ModNetworking.Serverbound<T> packet) {
        PayloadTypeRegistry.serverboundPlay().register(packet.type(), packet.codec());
        ServerPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) -> packet.handler().accept(payload, context.player()));
    }

    private static <T extends CustomPacketPayload> void registerClientReceiver(ModNetworking.Clientbound<T> packet) {
        ClientPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) -> ModNetworking.handleOnClient(payload));
    }
}
