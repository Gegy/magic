package dev.gegy.magic.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerLeaveEvent {
    Event<PlayerLeaveEvent> EVENT = EventFactory.createArrayBacked(PlayerLeaveEvent.class, events -> (player) -> {
        for (PlayerLeaveEvent event : events) {
            event.onPlayerLeave(player);
        }
    });

    void onPlayerLeave(ServerPlayer player);
}
