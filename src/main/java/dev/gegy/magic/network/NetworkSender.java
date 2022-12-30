package dev.gegy.magic.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface NetworkSender<T> {
    static <T, P> NetworkSender<P> of(final NetworkAddressing<T> addressing, final BiConsumer<T, P> sender) {
        return new NetworkSender<>() {
            @Override
            public void send(final P value) {
                addressing.send(target -> sender.accept(target, value));
            }

            @Override
            public void broadcast(final P value) {
                addressing.broadcast(target -> sender.accept(target, value));
            }

            @Override
            public void broadcastAndSend(final P value) {
                addressing.broadcastAndSend(target -> sender.accept(target, value));
            }
        };
    }

    void send(T value);

    void broadcast(T value);

    default void broadcastAndSend(final T value) {
        broadcast(value);
        send(value);
    }

    default <U> NetworkSender<U> map(final Function<U, T> function) {
        return new NetworkSender<>() {
            @Override
            public void send(final U value) {
                NetworkSender.this.send(function.apply(value));
            }

            @Override
            public void broadcast(final U value) {
                NetworkSender.this.broadcast(function.apply(value));
            }

            @Override
            public void broadcastAndSend(final U value) {
                NetworkSender.this.broadcastAndSend(function.apply(value));
            }
        };
    }
}
