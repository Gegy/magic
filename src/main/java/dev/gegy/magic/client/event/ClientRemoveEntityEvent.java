package dev.gegy.magic.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

public interface ClientRemoveEntityEvent {
    Event<ClientRemoveEntityEvent> EVENT = EventFactory.createArrayBacked(ClientRemoveEntityEvent.class, events -> entity -> {
        for (ClientRemoveEntityEvent event : events) {
            event.onRemoveEntity(entity);
        }
    });

    void onRemoveEntity(Entity entity);
}
