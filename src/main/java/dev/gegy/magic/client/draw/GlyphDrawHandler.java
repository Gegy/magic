package dev.gegy.magic.client.draw;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class GlyphDrawHandler {
    private GlyphDrawState drawState;

    private GlyphDrawHandler() {
    }

    public static void onInitialize() {
        GlyphDrawHandler drawHandler = new GlyphDrawHandler();
        ClientTickEvents.END_CLIENT_TICK.register(drawHandler::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player != null) {
            this.onDrawingTick(client.player);
        } else {
            this.drawState = null;
        }
    }

    private void onDrawingTick(ClientPlayerEntity player) {
        GlyphDrawState drawState = this.drawState;
        if (drawState == null) {
            this.drawState = drawState = new GlyphDrawState.Idle();
        }

        this.drawState = drawState.tick(player);
    }
}
