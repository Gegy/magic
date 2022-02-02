package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.client.casting.ClientCastingType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class ServerCastingBeam {
    public static final int MAXIMUM_LENGTH = 8;

    private final ServerPlayerEntity player;

    private ServerCastingBeam(ServerPlayerEntity player) {
        this.player = player;
    }

    public static ServerCasting build(ServerPlayerEntity player, ServerCastingBuilder casting) {
        var beam = new ServerCastingBeam(player);

        casting.registerClientCasting(ClientCastingType.BEAM, BeamParameters::new);
        casting.registerTicker(beam::tick);

        return casting.build();
    }

    @Nullable
    private ServerCasting.Factory tick() {
        // TODO: proper cancel logic
        if (this.player.isSneaking()) {
            return ServerCastingDrawing::build;
        }
        return null;
    }
}
