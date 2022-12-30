package dev.gegy.magic.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface LateTrackingEvent {
    Event<LateTrackingEvent> START = EventFactory.createArrayBacked(LateTrackingEvent.class, events -> (trackedEntity, player) -> {
        for (LateTrackingEvent event : events) {
            event.onStartTracking(trackedEntity, player);
        }
    });

    void onStartTracking(Entity trackedEntity, ServerPlayer player);
}
