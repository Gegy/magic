package dev.gegy.magic.client.casting.spell.beam;

import dev.gegy.magic.casting.spell.beam.BeamParameters;
import dev.gegy.magic.casting.spell.beam.ServerCastingBeam;
import dev.gegy.magic.casting.spell.beam.SetBeamActive;
import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendType;
import dev.gegy.magic.client.effect.casting.spell.SpellEffects;
import dev.gegy.magic.client.effect.casting.spell.beam.BeamEffect;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

public final class ClientCastingBeam {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private final PlayerEntity player;
    private final Spell spell;
    private final BeamEffect beam;
    private final EventSenders eventSenders;

    private ClientCastingBeam(PlayerEntity player, Spell spell, BeamEffect beam, EventSenders eventSenders) {
        this.player = player;
        this.spell = spell;
        this.beam = beam;
        this.eventSenders = eventSenders;
    }

    public static ClientCasting build(PlayerEntity player, BeamParameters parameters, ClientCastingBuilder casting) {
        var spell = casting.blendFrom(CastingBlendType.SPELL);
        if (spell == null) {
            return ClientCasting.NONE;
        }

        var beamEffect = casting.attachEffect(new BeamEffect(spell));
        beamEffect.setVisible(parameters.active());

        SpellEffects.attach(spell, casting);

        var eventSenders = EventSenders.register(casting);

        var beam = new ClientCastingBeam(player, spell, beamEffect, eventSenders);
        casting.bindInboundEvent(SetBeamActive.SPEC, beam::handleSetActive);

        casting.registerTicker(beam::tick);

        if (player.isMainPlayer()) {
            beam.bindInput(casting);
        }

        return casting.build();
    }

    private void bindInput(ClientCastingBuilder casting) {
        var active = new MutableBoolean();
        casting.registerTicker(() -> {
            boolean holdingKey = CLIENT.options.keyAttack.isPressed();
            if (active.booleanValue() != holdingKey) {
                active.setValue(holdingKey);
                this.eventSenders.setActive(holdingKey);
                this.beam.setVisible(holdingKey);
            }
        });
    }

    private void tick() {
        this.spell.tick();

        var direction = this.spell.transform().getDirection(1.0F);
        float maximumLength = ServerCastingBeam.MAXIMUM_LENGTH;

        var beamSource = this.getBeamSource();
        var beamTarget = beamSource.add(
                direction.getX() * maximumLength,
                direction.getY() * maximumLength,
                direction.getZ() * maximumLength
        );

        var cast = this.player.world.raycast(new RaycastContext(
                beamSource, beamTarget,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.ANY,
                this.player
        ));

        double length = cast.getPos().distanceTo(beamSource);
        this.beam.tick((float) length);
    }

    private void handleSetActive(SetBeamActive event) {
        this.beam.setVisible(event.active());
    }

    private Vec3d getBeamSource() {
        var transform = this.spell.transform();

        var source = new Vec3d(transform.getOrigin(1.0F));
        source = source.add(this.spell.source().getPosition(1.0F));

        return source;
    }

    private static record EventSenders(NetworkSender<SetBeamActive> setActive) {
        public static EventSenders register(ClientCastingBuilder casting) {
            return new EventSenders(
                    casting.registerOutboundEvent(SetBeamActive.SPEC)
            );
        }

        public void setActive(boolean active) {
            this.setActive.send(new SetBeamActive(active));
        }
    }
}
