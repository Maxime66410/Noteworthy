/**
 * File: NeoForgeNetwork.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.furranystudio.noteworthy.network.ModNetworking;

final class NeoForgeNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private NeoForgeNetwork() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        for (ModNetworking.Clientbound<?> packet : ModNetworking.clientboundPackets()) {
            addClientbound(registrar, packet);
        }
        for (ModNetworking.Serverbound<?> packet : ModNetworking.serverboundPackets()) {
            addServerbound(registrar, packet);
        }
    }

    static void initSender() {
        ModNetworking.init(new ModNetworking.Sender() {
            @Override
            public void toPlayer(ServerPlayer player, CustomPacketPayload payload) {
                PacketDistributor.sendToPlayer(player, payload);
            }

            @Override
            public void toServer(CustomPacketPayload payload) {
                ClientPacketDistributor.sendToServer(payload);
            }
        });
    }

    private static <T extends CustomPacketPayload> void addClientbound(PayloadRegistrar registrar, ModNetworking.Clientbound<T> packet) {
        registrar.playToClient(packet.type(), packet.codec(), (payload, context) -> ModNetworking.handleOnClient(payload));
    }

    private static <T extends CustomPacketPayload> void addServerbound(PayloadRegistrar registrar, ModNetworking.Serverbound<T> packet) {
        registrar.playToServer(packet.type(), packet.codec(),
            (payload, context) -> packet.handler().accept(payload, (ServerPlayer) context.player()));
    }
}
