package dev.gegy.magic.client.casting.spell.beam;

import dev.gegy.magic.casting.spell.beam.BeamParameters;
import dev.gegy.magic.casting.spell.beam.ServerCastingBeam;
import dev.gegy.magic.casting.spell.beam.SetBeamActive;
import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.effect.casting.spell.SpellEffects;
import dev.gegy.magic.client.effect.casting.spell.beam.BeamEffect;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Vector3f;

public final class ClientCastingBeam {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private final Player player;
    private final Spell spell;
    private final BeamEffect beam;
    private final EventSenders eventSenders;

    private ClientCastingBeam(final Player player, final Spell spell, final BeamEffect beam, final EventSenders eventSenders) {
        this.player = player;
        this.spell = spell;
        this.beam = beam;
        this.eventSenders = eventSenders;
    }

    public static ClientCasting build(final Player player, final BeamParameters parameters, final ClientCastingBuilder casting) {
        final Spell spell = parameters.spell()
                .blendOrCreate(player, casting, SpellTransformType.TRACKING);

        final BeamEffect beamEffect = casting.attachEffect(new BeamEffect(spell));
        beamEffect.setVisible(parameters.active());

        SpellEffects.attach(spell, casting);

        final EventSenders eventSenders = EventSenders.register(casting);

        final ClientCastingBeam beam = new ClientCastingBeam(player, spell, beamEffect, eventSenders);
        casting.bindInboundEvent(SetBeamActive.SPEC, beam::handleSetActive);

        casting.registerTicker(beam::tick);

        if (player.isLocalPlayer()) {
            beam.bindInput(casting);
        }

        return casting.build();
    }

    private void bindInput(final ClientCastingBuilder casting) {
        final MutableBoolean active = new MutableBoolean();
        casting.registerTicker(() -> {
            final boolean holdingKey = CLIENT.options.keyAttack.isDown();
            if (active.booleanValue() != holdingKey) {
                active.setValue(holdingKey);
                eventSenders.setActive(holdingKey);
                beam.setVisible(holdingKey);
            }
        });
    }

    private void tick() {
        spell.tick();

        final Vector3f direction = spell.transform().getDirection(1.0f);
        final float maximumLength = ServerCastingBeam.MAXIMUM_LENGTH;

        final Vec3 beamSource = getBeamSource();
        final Vec3 beamTarget = beamSource.add(
                direction.x() * maximumLength,
                direction.y() * maximumLength,
                direction.z() * maximumLength
        );

        final BlockHitResult cast = player.level.clip(new ClipContext(
                beamSource, beamTarget,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player
        ));

        final double length = cast.getLocation().distanceTo(beamSource);
        beam.tick((float) length);
    }

    private void handleSetActive(final SetBeamActive event) {
        beam.setVisible(event.active());
    }

    private Vec3 getBeamSource() {
        final SpellTransform transform = spell.transform();

        Vec3 source = new Vec3(transform.getOrigin(1.0f));
        source = source.add(spell.source().getPosition(1.0f));

        return source;
    }

    private record EventSenders(NetworkSender<SetBeamActive> setActive) {
        public static EventSenders register(final ClientCastingBuilder casting) {
            return new EventSenders(
                    casting.registerOutboundEvent(SetBeamActive.SPEC)
            );
        }

        public void setActive(final boolean active) {
            setActive.send(new SetBeamActive(active));
        }
    }
}
