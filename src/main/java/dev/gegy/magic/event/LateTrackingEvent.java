package dev.gegy.magic.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface LateTrackingEvent {
    Event<LateTrackingEvent> START = EventFactory.createArrayBacked(LateTrackingEvent.class, events -> (trackedEntity, player) -> {
        for (LateTrackingEvent event : events) {
            event.onStartTracking(trackedEntity, player);
        }
    });

    void onStartTracking(Entity trackedEntity, ServerPlayerEntity player);
}
