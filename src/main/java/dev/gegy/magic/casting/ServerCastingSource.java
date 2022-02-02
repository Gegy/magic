package dev.gegy.magic.casting;

import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.casting.event.EventSenderFactory;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import dev.gegy.magic.network.NetworkAddressing;
import dev.gegy.magic.network.NetworkSender;
import dev.gegy.magic.network.s2c.CastingEventS2CPacket;
import dev.gegy.magic.network.s2c.SetCastingS2CPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class ServerCastingSource implements AutoCloseable {
    private final ServerPlayerEntity player;
    private ServerCasting casting;

    private final NetworkAddressing<ServerPlayerEntity> addressing;

    public ServerCastingSource(ServerPlayerEntity player) {
        this.player = player;

        this.addressing = NetworkAddressing.trackingClients(player);
    }

    public ServerPlayerEntity player() {
        return this.player;
    }

    public void setCasting(@Nullable ServerCasting.Factory factory) {
        if (factory != null) {
            var casting = factory.build(this.player, this.createCastingBuilder());
            this.casting = casting;
            this.notifyClientCasting(casting.createClientCasting());
        } else {
            this.casting = null;
            this.notifyClientCasting(null);
        }
    }

    private ServerCastingBuilder createCastingBuilder() {
        var addressing = this.addressing;
        var player = this.player;

        var eventSender = addressing.sender(CastingEventS2CPacket::sendTo);
        return new ServerCastingBuilder(new EventSenderFactory() {
            @Override
            public <T> NetworkSender<T> create(CastingEventSpec<T> spec) {
                return eventSender.map(event -> CastingEventS2CPacket.create(player, spec, event));
            }
        });
    }

    private void notifyClientCasting(@Nullable ConfiguredClientCasting<?> clientCasting) {
        this.addressing.sender(SetCastingS2CPacket::sendTo)
                .broadcastAndSend(SetCastingS2CPacket.create(this.player, clientCasting));
    }

    public void tick() {
        var casting = this.casting;
        if (casting != null) {
            this.tickCasting(casting);
        }
    }

    private void tickCasting(ServerCasting casting) {
        var nextCasting = casting.tick();
        if (nextCasting != null) {
            this.setCasting(nextCasting);
        }
    }

    public void handleEvent(Identifier id, PacketByteBuf buf) {
        var casting = this.casting;
        if (casting != null) {
            casting.handleEvent(id, buf);
        }
    }

    public void onStartTracking(ServerPlayerEntity player) {
        var casting = this.casting;
        if (casting != null) {
            var packet = SetCastingS2CPacket.create(this.player, casting.createClientCasting());
            SetCastingS2CPacket.sendTo(player, packet);
        }
    }

    public void onStopTracking(ServerPlayerEntity player) {
    }

    @Override
    public void close() {
        this.setCasting(null);
    }
}
