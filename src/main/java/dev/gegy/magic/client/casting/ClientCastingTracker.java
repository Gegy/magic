package dev.gegy.magic.client.casting;

import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.event.ClientRemoveEntityEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClientCastingTracker {
    public static final ClientCastingTracker INSTANCE = new ClientCastingTracker();

    private final Int2ObjectMap<ClientCastingSource> sources = new Int2ObjectOpenHashMap<>();

    private ClientCastingTracker() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
        ClientRemoveEntityEvent.EVENT.register(INSTANCE::removeSource);
    }

    public EffectSelector effectSelectorFor(Player player) {
        var source = this.getSource(player);
        return source != null ? source.effectSelector() : EffectSelector.EMPTY;
    }

    public void handleEvent(Player entity, ResourceLocation id, FriendlyByteBuf buf) {
        var source = this.getOrCreateSource(entity);
        source.handleEvent(id, buf);
    }

    public void setCasting(Player player, @Nullable ConfiguredClientCasting<?> casting) {
        var source = this.getOrCreateSource(player);
        source.handleServerCast(casting);
    }

    @NotNull
    private ClientCastingSource getOrCreateSource(Player player) {
        return this.sources.computeIfAbsent(player.getId(), i -> new ClientCastingSource(player));
    }

    @Nullable
    private ClientCastingSource getSource(Player player) {
        return this.sources.get(player.getId());
    }

    private void tick(Minecraft client) {
        var player = client.player;
        if (player == null) {
            this.clearSources();
            return;
        }

        for (var source : this.sources.values()) {
            source.tick();
        }
    }

    private void clearSources() {
        if (!this.sources.isEmpty()) {
            for (var source : this.sources.values()) {
                source.close();
            }
            this.sources.clear();
        }
    }

    private void removeSource(Entity entity) {
        var source = this.sources.remove(entity.getId());
        if (source != null) {
            source.close();
        }
    }
}
