package dev.gegy.magic.client.casting;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.casting.blend.CastingBlender;
import dev.gegy.magic.client.effect.EffectSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface ClientCasting {
    ClientCasting NONE = new ClientCasting() {
        @Override
        public ClientCasting tick() {
            return this;
        }

        @Override
        public void handleEvent(Identifier id, PacketByteBuf buf) {
            Magic.LOGGER.warn("Received unexpected inbound casting event '{}' while no casting is active", id);
        }

        @Override
        public EffectSelector getEffects() {
            return EffectSelector.EMPTY;
        }
    };

    default ClientCasting handleServerCast(ClientCasting casting) {
        return casting;
    }

    ClientCasting tick();

    void handleEvent(Identifier id, PacketByteBuf buf);

    EffectSelector getEffects();

    default CastingBlender getBlender() {
        return CastingBlender.EMPTY;
    }

    interface Factory<P> {
        ClientCasting build(PlayerEntity player, P parameters, ClientCastingBuilder casting);
    }
}
