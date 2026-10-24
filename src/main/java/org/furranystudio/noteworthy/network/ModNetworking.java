/**
 * File: ModNetworking.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.noteworthy.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ModNetworking {

    public record Clientbound<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                             StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
    }

    public record Serverbound<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                             StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                             BiConsumer<T, ServerPlayer> handler) {
    }

    public interface Sender {
        void toPlayer(ServerPlayer player, CustomPacketPayload payload);

        void toServer(CustomPacketPayload payload);
    }

    private static final List<Clientbound<?>> CLIENTBOUND = new ArrayList<>();
    private static final List<Serverbound<?>> SERVERBOUND = new ArrayList<>();
    private static final Map<CustomPacketPayload.Type<?>, Consumer<?>> CLIENT_HANDLERS = new HashMap<>();
    private static Sender sender;

    private ModNetworking() {
    }

    public static <T extends CustomPacketPayload> void clientbound(CustomPacketPayload.Type<T> type,
                                                                   StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        CLIENTBOUND.add(new Clientbound<>(type, codec));
    }

    public static <T extends CustomPacketPayload> void serverbound(CustomPacketPayload.Type<T> type,
                                                                   StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                   BiConsumer<T, ServerPlayer> handler) {
        SERVERBOUND.add(new Serverbound<>(type, codec, handler));
    }

    // Client handlers are set from client code only, so a dedicated server never loads them
    public static <T extends CustomPacketPayload> void setClientHandler(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
        CLIENT_HANDLERS.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void handleOnClient(T payload) {
        Consumer<T> handler = (Consumer<T>) CLIENT_HANDLERS.get(payload.type());
        if (handler != null) {
            handler.accept(payload);
        }
    }

    public static List<Clientbound<?>> clientboundPackets() {
        return CLIENTBOUND;
    }

    public static List<Serverbound<?>> serverboundPackets() {
        return SERVERBOUND;
    }

    public static void init(Sender sender) {
        ModNetworking.sender = sender;
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        sender.toPlayer(player, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        sender.toServer(payload);
    }
}
