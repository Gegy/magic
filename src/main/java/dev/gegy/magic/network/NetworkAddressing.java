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
            public void send(final Consumer<Unit> handler) {
                handler.accept(Unit.INSTANCE);
            }

            @Override
            public void broadcast(final Consumer<Unit> handler) {
            }
        };
    }

    static NetworkAddressing<ServerPlayer> trackingClients(final ServerPlayer player) {
        return new NetworkAddressing<>() {
            @Override
            public void send(final Consumer<ServerPlayer> handler) {
                handler.accept(player);
            }

            @Override
            public void broadcast(final Consumer<ServerPlayer> handler) {
                for (final ServerPlayer tracking : PlayerLookup.tracking(player)) {
                    handler.accept(tracking);
                }
            }
        };
    }

    void send(Consumer<T> handler);

    void broadcast(Consumer<T> handler);

    default void broadcastAndSend(final Consumer<T> handler) {
        broadcast(handler);
        send(handler);
    }

    default <P> NetworkSender<P> sender(final BiConsumer<T, P> sender) {
        return NetworkSender.of(this, sender);
    }
}
