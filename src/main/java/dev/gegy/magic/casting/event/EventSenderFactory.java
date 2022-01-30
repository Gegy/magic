package dev.gegy.magic.casting.event;

import dev.gegy.magic.network.NetworkSender;

public interface EventSenderFactory {
    <T> NetworkSender<T> create(CastingEventSpec<T> spec);
}
