package dev.gegy.magic.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface NetworkAddressing<T> {
    static NetworkAddressing<Unit> server() {
        return new NetworkAddressing<>() {
            @Override
            public void send(Consumer<Unit> handler) {
                handler.accept(Unit.INSTANCE);
            }

            @Override
            public void broadcast(Consumer<Unit> handler) {
            }
        };
    }

    static NetworkAddressing<ServerPlayer> trackingClients(ServerPlayer player) {
        return new NetworkAddressing<>() {
            @Override
            public void send(Consumer<ServerPlayer> handler) {
                handler.accept(player);
            }

            @Override
            public void broadcast(Consumer<ServerPlayer> handler) {
                for (var tracking : PlayerLookup.tracking(player)) {
                    handler.accept(tracking);
                }
            }
        };
    }

    void send(Consumer<T> handler);

    void broadcast(Consumer<T> handler);

    default void broadcastAndSend(Consumer<T> handler) {
        this.broadcast(handler);
        this.send(handler);
    }

    default <P> NetworkSender<P> sender(BiConsumer<T, P> sender) {
        return NetworkSender.of(this, sender);
    }
}
