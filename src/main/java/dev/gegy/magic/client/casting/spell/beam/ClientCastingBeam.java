package dev.gegy.magic.client.casting.spell.beam;

import dev.gegy.magic.casting.spell.beam.BeamParameters;
import dev.gegy.magic.casting.spell.beam.ServerCastingBeam;
import dev.gegy.magic.casting.spell.beam.SetBeamActive;
import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.effect.casting.spell.SpellEffects;
import dev.gegy.magic.client.effect.casting.spell.beam.BeamEffect;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

public final class ClientCastingBeam {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private final Player player;
    private final Spell spell;
    private final BeamEffect beam;
    private final EventSenders eventSenders;

    private ClientCastingBeam(Player player, Spell spell, BeamEffect beam, EventSenders eventSenders) {
        this.player = player;
        this.spell = spell;
        this.beam = beam;
        this.eventSenders = eventSenders;
    }

    public static ClientCasting build(Player player, BeamParameters parameters, ClientCastingBuilder casting) {
        var spell = parameters.spell()
                .blendOrCreate(player, casting, SpellTransformType.TRACKING);

        var beamEffect = casting.attachEffect(new BeamEffect(spell));
        beamEffect.setVisible(parameters.active());

        SpellEffects.attach(spell, casting);

        var eventSenders = EventSenders.register(casting);

        var beam = new ClientCastingBeam(player, spell, beamEffect, eventSenders);
        casting.bindInboundEvent(SetBeamActive.SPEC, beam::handleSetActive);

        casting.registerTicker(beam::tick);

        if (player.isLocalPlayer()) {
            beam.bindInput(casting);
        }

        return casting.build();
    }

    private void bindInput(ClientCastingBuilder casting) {
        var active = new MutableBoolean();
        casting.registerTicker(() -> {
            boolean holdingKey = CLIENT.options.keyAttack.isDown();
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
                direction.x() * maximumLength,
                direction.y() * maximumLength,
                direction.z() * maximumLength
        );

        var cast = this.player.level.clip(new ClipContext(
                beamSource, beamTarget,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                this.player
        ));

        double length = cast.getLocation().distanceTo(beamSource);
        this.beam.tick((float) length);
    }

    private void handleSetActive(SetBeamActive event) {
        this.beam.setVisible(event.active());
    }

    private Vec3 getBeamSource() {
        var transform = this.spell.transform();

        var source = new Vec3(transform.getOrigin(1.0F));
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
