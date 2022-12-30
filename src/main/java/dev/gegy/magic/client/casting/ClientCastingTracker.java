package dev.gegy.magic.client.casting;

import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.event.ClientRemoveEntityEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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

    public EffectSelector effectSelectorFor(final Player player) {
        final ClientCastingSource source = getSource(player);
        return source != null ? source.effectSelector() : EffectSelector.EMPTY;
    }

    public void handleEvent(final Player entity, final ResourceLocation id, final FriendlyByteBuf buf) {
        final ClientCastingSource source = getOrCreateSource(entity);
        source.handleEvent(id, buf);
    }

    public void setCasting(final Player player, @Nullable final ConfiguredClientCasting<?> casting) {
        final ClientCastingSource source = getOrCreateSource(player);
        source.handleServerCast(casting);
    }

    @NotNull
    private ClientCastingSource getOrCreateSource(final Player player) {
        return sources.computeIfAbsent(player.getId(), i -> new ClientCastingSource(player));
    }

    @Nullable
    private ClientCastingSource getSource(final Player player) {
        return sources.get(player.getId());
    }

    private void tick(final Minecraft client) {
        final LocalPlayer player = client.player;
        if (player == null) {
            clearSources();
            return;
        }

        for (final ClientCastingSource source : sources.values()) {
            source.tick();
        }
    }

    private void clearSources() {
        if (!sources.isEmpty()) {
            for (final ClientCastingSource source : sources.values()) {
                source.close();
            }
            sources.clear();
        }
    }

    private void removeSource(final Entity entity) {
        final ClientCastingSource source = sources.remove(entity.getId());
        if (source != null) {
            source.close();
        }
    }
}
