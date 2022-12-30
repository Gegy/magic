package dev.gegy.magic.client.casting;

import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.client.casting.blend.CastingBlender;
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

    public ClientCastingSource(Player player) {
        this.player = player;
    }

    public <P> void handleServerCast(@Nullable ConfiguredClientCasting<P> configuredCasting) {
        var blender = this.casting.getBlender();

        var builder = this.createCastingBuilder(blender);
        if (configuredCasting != null) {
            this.handleServerCast(configuredCasting.build(this.player, builder));
        } else {
            this.handleServerCast(builder.build());
        }
    }

    private void handleServerCast(ClientCasting casting) {
        this.setCasting(this.casting.handleServerCast(casting));
    }

    @NotNull
    private ClientCastingBuilder createCastingBuilder(CastingBlender blender) {
        return new ClientCastingBuilder(ClientCastingSource::createEventSender, blender);
    }

    private static <T> NetworkSender<T> createEventSender(CastingEventSpec<T> spec) {
        return NetworkAddressing.server().sender(($, event) -> CastingEventC2SPacket.sendToServer(spec, event));
    }

    private void setCasting(ClientCasting newCasting) {
        var oldCasting = this.casting;
        this.closeCasting(oldCasting);
        this.setupCasting(newCasting);

        this.casting = newCasting;
    }

    private void setupCasting(ClientCasting casting) {
        var effectManager = EffectManager.get();
        for (var effect : casting.getEffects()) {
            effectManager.add(effect);
        }
    }

    private void closeCasting(ClientCasting casting) {
        var effectManager = EffectManager.get();
        for (var effect : casting.getEffects()) {
            effectManager.remove(effect);
        }
    }

    public void tick() {
        var casting = this.casting;
        var nextCasting = casting.tick();
        if (nextCasting != casting) {
            this.setCasting(nextCasting);
        }
    }

    public void handleEvent(ResourceLocation id, FriendlyByteBuf buf) {
        this.casting.handleEvent(id, buf);
    }

    public EffectSelector effectSelector() {
        return this.casting.getEffects();
    }

    @Override
    public void close() {
        this.setCasting(ClientCasting.NONE);
    }
}
