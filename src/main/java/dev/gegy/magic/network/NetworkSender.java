package dev.gegy.magic.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface NetworkSender<T> {
    static <T, P> NetworkSender<P> of(NetworkAddressing<T> addressing, BiConsumer<T, P> sender) {
        return new NetworkSender<>() {
            @Override
            public void send(P value) {
                addressing.send(target -> sender.accept(target, value));
            }

            @Override
            public void broadcast(P value) {
                addressing.broadcast(target -> sender.accept(target, value));
            }

            @Override
            public void broadcastAndSend(P value) {
                addressing.broadcastAndSend(target -> sender.accept(target, value));
            }
        };
    }

    void send(T value);

    void broadcast(T value);

    default void broadcastAndSend(T value) {
        this.broadcast(value);
        this.send(value);
    }

    default <U> NetworkSender<U> map(Function<U, T> function) {
        return new NetworkSender<>() {
            @Override
            public void send(U value) {
                NetworkSender.this.send(function.apply(value));
            }

            @Override
            public void broadcast(U value) {
                NetworkSender.this.broadcast(function.apply(value));
            }

            @Override
            public void broadcastAndSend(U value) {
                NetworkSender.this.broadcastAndSend(function.apply(value));
            }
        };
    }
}
