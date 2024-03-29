package dev.gegy.magic.client.casting;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.drawing.DrawingParameters;
import dev.gegy.magic.casting.spell.beam.BeamParameters;
import dev.gegy.magic.casting.spell.teleport.TeleportParameters;
import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.spell.beam.ClientCastingBeam;
import dev.gegy.magic.client.casting.spell.teleport.ClientCastingTeleport;
import dev.gegy.magic.network.codec.PacketCodec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public final class ClientCastingType<P> {
    public static final MappedRegistry<ClientCastingType<?>> REGISTRY = FabricRegistryBuilder.createSimple(ClientCastingType.type(), Magic.identifier("casting"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final PacketCodec<ClientCastingType<?>> PACKET_CODEC = PacketCodec.ofRegistry(REGISTRY);

    public static final ClientCastingType<DrawingParameters> DRAWING = register("drawing", ClientCastingDrawing::build, DrawingParameters.CODEC);
    public static final ClientCastingType<BeamParameters> BEAM = register("beam", ClientCastingBeam::build, BeamParameters.CODEC);
    public static final ClientCastingType<TeleportParameters> TELEPORT = register("teleport", ClientCastingTeleport::build, TeleportParameters.CODEC);

    private final ClientCasting.Factory<P> factory;
    private final PacketCodec<P> parametersCodec;

    public ClientCastingType(final ClientCasting.Factory<P> factory, final PacketCodec<P> parametersCodec) {
        this.factory = factory;
        this.parametersCodec = parametersCodec;
    }

    public static void onInitialize() {
    }

    private static <P> ClientCastingType<P> register(final String id, final ClientCasting.Factory<P> factory, final PacketCodec<P> parametersCodec) {
        return Registry.register(REGISTRY, Magic.identifier(id), new ClientCastingType<>(factory, parametersCodec));
    }

    public ClientCasting.Factory<P> factory() {
        return factory;
    }

    public PacketCodec<P> parametersCodec() {
        return parametersCodec;
    }

    public ConfiguredClientCasting<P> configure(final P parameters) {
        return new ConfiguredClientCasting<>(this, parameters);
    }

    @SuppressWarnings("unchecked")
    private static Class<ClientCastingType<?>> type() {
        return (Class<ClientCastingType<?>>) (Object) ClientCastingType.class;
    }
}
