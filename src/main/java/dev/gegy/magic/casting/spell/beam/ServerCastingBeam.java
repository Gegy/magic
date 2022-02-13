package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class ServerCastingBeam {
    public static final int MAXIMUM_LENGTH = 8;

    private final ServerPlayerEntity player;
    private final SpellParameters spell;
    private final EventSenders eventSenders;

    private boolean active;

    private ServerCastingBeam(ServerPlayerEntity player, SpellParameters spell, EventSenders eventSenders) {
        this.player = player;
        this.spell = spell;
        this.eventSenders = eventSenders;
    }

    public static ServerCasting build(ServerPlayerEntity player, SpellParameters spell, ServerCastingBuilder casting) {
        var eventSenders = EventSenders.register(casting);
        var beam = new ServerCastingBeam(player, spell, eventSenders);

        casting.registerClientCasting(ClientCastingType.BEAM, beam::buildParameters);
        casting.registerTicker(beam::tick);

        casting.bindInboundEvent(SetBeamActive.SPEC, beam::handleSetActive);

        return casting.build();
    }

    private void handleSetActive(SetBeamActive event) {
        this.active = event.active();
        this.eventSenders.setActive(event.active());
    }

    @Nullable
    private ServerCasting.Factory tick() {
        // TODO: proper cancel logic
        if (this.player.isSneaking() && !this.active) {
            return ServerCastingDrawing::build;
        }
        return null;
    }

    private BeamParameters buildParameters() {
        return new BeamParameters(this.spell, this.active);
    }

    private static record EventSenders(NetworkSender<SetBeamActive> setActive) {
        public static EventSenders register(ServerCastingBuilder casting) {
            return new EventSenders(
                    casting.registerOutboundEvent(SetBeamActive.SPEC)
            );
        }

        public void setActive(boolean active) {
            this.setActive.broadcast(new SetBeamActive(active));
        }
    }
}
