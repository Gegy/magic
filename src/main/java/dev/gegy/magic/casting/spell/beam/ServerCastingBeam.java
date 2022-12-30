package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class ServerCastingBeam {
    public static final int MAXIMUM_LENGTH = 8;

    private final ServerPlayer player;
    private final SpellParameters spell;
    private final EventSenders eventSenders;

    private boolean active;

    private ServerCastingBeam(final ServerPlayer player, final SpellParameters spell, final EventSenders eventSenders) {
        this.player = player;
        this.spell = spell;
        this.eventSenders = eventSenders;
    }

    public static ServerCasting build(final ServerPlayer player, final SpellParameters spell, final ServerCastingBuilder casting) {
        final EventSenders eventSenders = EventSenders.register(casting);
        final ServerCastingBeam beam = new ServerCastingBeam(player, spell, eventSenders);

        casting.registerClientCasting(ClientCastingType.BEAM, beam::buildParameters);
        casting.registerTicker(beam::tick);

        casting.bindInboundEvent(SetBeamActive.SPEC, beam::handleSetActive);

        return casting.build();
    }

    private void handleSetActive(final SetBeamActive event) {
        active = event.active();
        eventSenders.setActive(event.active());
    }

    @Nullable
    private ServerCasting.Factory tick() {
        // TODO: proper cancel logic
        if (player.isShiftKeyDown() && !active) {
            return ServerCastingDrawing::build;
        }
        return null;
    }

    private BeamParameters buildParameters() {
        return new BeamParameters(spell, active);
    }

    private record EventSenders(NetworkSender<SetBeamActive> setActive) {
        public static EventSenders register(final ServerCastingBuilder casting) {
            return new EventSenders(
                    casting.registerOutboundEvent(SetBeamActive.SPEC)
            );
        }

        public void setActive(final boolean active) {
            setActive.broadcast(new SetBeamActive(active));
        }
    }
}
