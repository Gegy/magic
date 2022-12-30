package dev.gegy.magic.client.casting;

import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.client.casting.blend.CastingBlender;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectManager;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.network.NetworkAddressing;
import dev.gegy.magic.network.NetworkSender;
import dev.gegy.magic.network.c2s.CastingEventC2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClientCastingSource implements AutoCloseable {
    private final Player player;

    private ClientCasting casting = ClientCasting.NONE;

    public ClientCastingSource(final Player player) {
        this.player = player;
    }

    public <P> void handleServerCast(@Nullable final ConfiguredClientCasting<P> configuredCasting) {
        final CastingBlender blender = casting.getBlender();

        final ClientCastingBuilder builder = createCastingBuilder(blender);
        if (configuredCasting != null) {
            handleServerCast(configuredCasting.build(player, builder));
        } else {
            handleServerCast(builder.build());
        }
    }

    private void handleServerCast(final ClientCasting casting) {
        setCasting(this.casting.handleServerCast(casting));
    }

    @NotNull
    private ClientCastingBuilder createCastingBuilder(final CastingBlender blender) {
        return new ClientCastingBuilder(ClientCastingSource::createEventSender, blender);
    }

    private static <T> NetworkSender<T> createEventSender(final CastingEventSpec<T> spec) {
        return NetworkAddressing.server().sender(($, event) -> CastingEventC2SPacket.sendToServer(spec, event));
    }

    private void setCasting(final ClientCasting newCasting) {
        final ClientCasting oldCasting = casting;
        closeCasting(oldCasting);
        setupCasting(newCasting);

        casting = newCasting;
    }

    private void setupCasting(final ClientCasting casting) {
        final EffectManager effectManager = EffectManager.get();
        for (final Effect effect : casting.getEffects()) {
            effectManager.add(effect);
        }
    }

    private void closeCasting(final ClientCasting casting) {
        final EffectManager effectManager = EffectManager.get();
        for (final Effect effect : casting.getEffects()) {
            effectManager.remove(effect);
        }
    }

    public void tick() {
        final ClientCasting casting = this.casting;
        final ClientCasting nextCasting = casting.tick();
        if (nextCasting != casting) {
            setCasting(nextCasting);
        }
    }

    public void handleEvent(final ResourceLocation id, final FriendlyByteBuf buf) {
        casting.handleEvent(id, buf);
    }

    public EffectSelector effectSelector() {
        return casting.getEffects();
    }

    @Override
    public void close() {
        setCasting(ClientCasting.NONE);
    }
}
