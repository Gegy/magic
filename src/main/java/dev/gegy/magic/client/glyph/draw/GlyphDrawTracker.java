package dev.gegy.magic.client.glyph.draw;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class GlyphDrawTracker {
    private GlyphDrawState state;

    private GlyphDrawTracker() {
    }

    public static void onInitialize() {
        GlyphDrawTracker drawTracker = new GlyphDrawTracker();
        ClientTickEvents.END_CLIENT_TICK.register(drawTracker::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player != null) {
            this.onDrawingTick(client.player);
        } else {
            this.state = null;
        }
    }

    private void onDrawingTick(ClientPlayerEntity player) {
        GlyphDrawState state = this.state;
        if (state == null) {
            this.state = state = new IdleGlyphDrawState();
        }

        this.state = state.tick(player);
    }
}
