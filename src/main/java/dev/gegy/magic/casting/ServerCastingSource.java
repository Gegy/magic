package dev.gegy.magic.casting;

import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.casting.event.EventSenderFactory;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import dev.gegy.magic.network.NetworkAddressing;
import dev.gegy.magic.network.NetworkSender;
import dev.gegy.magic.network.s2c.CastingEventS2CPacket;
import dev.gegy.magic.network.s2c.SetCastingS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class ServerCastingSource implements AutoCloseable {
    private final ServerPlayer player;
    private ServerCasting casting;

    private final NetworkAddressing<ServerPlayer> addressing;

    public ServerCastingSource(final ServerPlayer player) {
        this.player = player;

        addressing = NetworkAddressing.trackingClients(player);
    }

    public ServerPlayer player() {
        return player;
    }

    public void setCasting(@Nullable final ServerCasting.Factory factory) {
        if (factory != null) {
            final ServerCasting casting = factory.build(player, createCastingBuilder());
            this.casting = casting;
            notifyClientCasting(casting.createClientCasting());
        } else {
            casting = null;
            notifyClientCasting(null);
        }
    }

    private ServerCastingBuilder createCastingBuilder() {
        final NetworkAddressing<ServerPlayer> addressing = this.addressing;
        final ServerPlayer player = this.player;

        final NetworkSender<FriendlyByteBuf> eventSender = addressing.sender(CastingEventS2CPacket::sendTo);
        return new ServerCastingBuilder(new EventSenderFactory() {
            @Override
            public <T> NetworkSender<T> create(final CastingEventSpec<T> spec) {
                return eventSender.map(event -> CastingEventS2CPacket.create(player, spec, event));
            }
        });
    }

    private void notifyClientCasting(@Nullable final ConfiguredClientCasting<?> clientCasting) {
        addressing.sender(SetCastingS2CPacket::sendTo)
                .broadcastAndSend(SetCastingS2CPacket.create(player, clientCasting));
    }

    public void tick() {
        final ServerCasting casting = this.casting;
        if (casting != null) {
            tickCasting(casting);
        }
    }

    private void tickCasting(final ServerCasting casting) {
        final ServerCasting.Factory nextCasting = casting.tick();
        if (nextCasting != null) {
            setCasting(nextCasting);
        }
    }

    public void handleEvent(final ResourceLocation id, final FriendlyByteBuf buf) {
        final ServerCasting casting = this.casting;
        if (casting != null) {
            casting.handleEvent(id, buf);
        }
    }

    public void onStartTracking(final ServerPlayer player) {
        final ServerCasting casting = this.casting;
        if (casting != null) {
            final FriendlyByteBuf packet = SetCastingS2CPacket.create(this.player, casting.createClientCasting());
            SetCastingS2CPacket.sendTo(player, packet);
        }
    }

    public void onStopTracking(final ServerPlayer player) {
    }

    @Override
    public void close() {
        setCasting(null);
    }
}
