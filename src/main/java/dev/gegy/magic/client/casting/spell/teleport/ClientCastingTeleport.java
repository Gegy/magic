package dev.gegy.magic.client.casting.spell.teleport;

import dev.gegy.magic.casting.spell.teleport.SelectTeleportTarget;
import dev.gegy.magic.casting.spell.teleport.TeleportInput;
import dev.gegy.magic.casting.spell.teleport.TeleportParameters;
import dev.gegy.magic.casting.spell.teleport.TeleportTarget;
import dev.gegy.magic.casting.spell.teleport.TeleportTargetSymbol;
import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.effect.casting.spell.SpellEffects;
import dev.gegy.magic.client.effect.casting.spell.teleport.TeleportEffect;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;

public final class ClientCastingTeleport {
    private final TeleportEffect effect;

    private final Map<UUID, TeleportTargetSymbol> targets;
    private TeleportTarget selectedTarget;

    private final EventSenders eventSenders;

    private ClientCastingTeleport(final TeleportEffect effect, final Map<UUID, TeleportTargetSymbol> targets, final EventSenders eventSenders) {
        this.effect = effect;
        this.targets = targets;
        this.eventSenders = eventSenders;
    }

    public static ClientCasting build(final Player player, final TeleportParameters parameters, final ClientCastingBuilder casting) {
        final Spell spell = parameters.spell()
                .blendOrCreate(player, casting, SpellTransformType.FIXED);

        final SpellCastingGlyph sourceGlyph = spell.glyphs().get(0);

        final TeleportEffect effect = casting.attachEffect(TeleportEffect.create(
                sourceGlyph,
                parameters.targets().values(),
                player.level.getGameTime()
        ));
        SpellEffects.attach(spell, casting);

        final EventSenders eventSenders = EventSenders.register(casting);

        final ClientCastingTeleport teleport = new ClientCastingTeleport(effect, parameters.targets(), eventSenders);

        casting.registerTicker(teleport::tick);

        if (player.isLocalPlayer()) {
            teleport.bindInput(player, casting);
        }

        return casting.build();
    }

    private void bindInput(final Player player, final ClientCastingBuilder casting) {
        final TeleportInput input = new TeleportInput();
        casting.registerTicker(() -> {
            final TeleportTarget target = input.tick(this, player);
            if (target != null) {
                selectTarget(target);
            }
        });
    }

    private void tick() {

    }

    private void selectTarget(final TeleportTarget target) {
        selectedTarget = target;
        eventSenders.selectTarget(target);
    }

    private record EventSenders(NetworkSender<SelectTeleportTarget> selectTarget) {
        private static EventSenders register(final ClientCastingBuilder casting) {
            return new EventSenders(
                    casting.registerOutboundEvent(SelectTeleportTarget.SPEC)
            );
        }

        public void selectTarget(final TeleportTarget target) {
            selectTarget.send(new SelectTeleportTarget(target.id()));
        }
    }
}
