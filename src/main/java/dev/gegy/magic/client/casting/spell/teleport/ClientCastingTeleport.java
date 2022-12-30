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

    private ClientCastingTeleport(TeleportEffect effect, Map<UUID, TeleportTargetSymbol> targets, EventSenders eventSenders) {
        this.effect = effect;
        this.targets = targets;
        this.eventSenders = eventSenders;
    }

    public static ClientCasting build(Player player, TeleportParameters parameters, ClientCastingBuilder casting) {
        var spell = parameters.spell()
                .blendOrCreate(player, casting, SpellTransformType.FIXED);

        var sourceGlyph = spell.glyphs().get(0);

        var effect = casting.attachEffect(TeleportEffect.create(
                sourceGlyph,
                parameters.targets().values(),
                player.level.getGameTime()
        ));
        SpellEffects.attach(spell, casting);

        var eventSenders = EventSenders.register(casting);

        var teleport = new ClientCastingTeleport(effect, parameters.targets(), eventSenders);

        casting.registerTicker(teleport::tick);

        if (player.isLocalPlayer()) {
            teleport.bindInput(player, casting);
        }

        return casting.build();
    }

    private void bindInput(Player player, ClientCastingBuilder casting) {
        var input = new TeleportInput();
        casting.registerTicker(() -> {
            var target = input.tick(this, player);
            if (target != null) {
                this.selectTarget(target);
            }
        });
    }

    private void tick() {

    }

    private void selectTarget(TeleportTarget target) {
        this.selectedTarget = target;
        this.eventSenders.selectTarget(target);
    }

    private static record EventSenders(NetworkSender<SelectTeleportTarget> selectTarget) {
        private static EventSenders register(ClientCastingBuilder casting) {
            return new EventSenders(
                    casting.registerOutboundEvent(SelectTeleportTarget.SPEC)
            );
        }

        public void selectTarget(TeleportTarget target) {
            this.selectTarget.send(new SelectTeleportTarget(target.id()));
        }
    }
}
