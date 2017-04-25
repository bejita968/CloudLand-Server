package org.dragonet.cloudland.server.network;

import org.dragonet.cloudland.net.protocol.Initial;
import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;
import org.dragonet.cloudland.server.network.protocol.MessageWithHandler;
import com.google.protobuf.Message;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on 2016/11/27.
 */
public class CLSession implements Session {

    private final NetworkServer network;
    private final SocketChannel channel;

    private LinkedBlockingQueue<MessageWithHandler> incomingMessages = new LinkedBlockingQueue<>();

    private String networkUniqueId;
    private PlayerEntity player;

    @Getter
    private boolean disconnected;

    // Settings

    private final Map<SessionOptions, Object> options = new HashMap<>();

    @Getter
    private int viewDistance = 6;

    @Getter
    private int viewDistanceSquared = 36;

    @Getter
    private int blockViewDistanceSquared = 36 * 512;

    public CLSession(NetworkServer network, String networkUniqueId, SocketChannel channel) {
        this.network = network;
        this.networkUniqueId = networkUniqueId;
        this.channel = channel;
    }

    public void putIncomingMessage(Message message, CLMessageHandler<Message> handler){
        incomingMessages.add(new MessageWithHandler(message, handler));
    }

    @Override
    public CloudLandServer getServer() {
        return network.getServer();
    }

    public String getUniqueIdentifier() {
        return networkUniqueId;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public NetworkServer getNetworkServer() {
        return network;
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public boolean isAuthenticated() {
        return player != null;
    }

    @Override
    public int hashCode() {
        return networkUniqueId.hashCode();
    }

    @Override
    public void disconnect(String reason) {
        sendNetworkMessage(Initial.ServerDisconnectMessage.newBuilder()
        .setReason(reason).build());
        network.closeSession(this, reason);
    }

    @Override
    public <T> T getOption(SessionOptions key, T def) {
        if(!options.containsKey(key)) return def;
        return (T) options.get(key);
    }

    @Override
    public void setOption(SessionOptions key, Object value) {
        options.put(key, value);
    }

    @Override
    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        this.viewDistanceSquared = viewDistance * viewDistance;
        this.blockViewDistanceSquared = viewDistanceSquared * 512;
    }

    /**
     * Called BEFORE disconnecting, clean up local references and stuffs.
     */
    public void onDisconnect(String reason){
        if(!isAuthenticated() || disconnected) return;
        disconnected = true;
        if(player.getMap() == null) return;
        player.getMap().removeEntity(player);
        player.setMap(null);
        player = null;
    }


    @Override
    public void sendNetworkMessage(Message message) {
        if(message == null) return;
        channel.writeAndFlush(message);
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(PlayerEntity player) {
        if(this.player != null) {
            throw new IllegalStateException("player already set! ");
        }
        this.player = player;
    }

    public void processMessages(){
        while(!incomingMessages.isEmpty()) {
            MessageWithHandler m = incomingMessages.poll();
            m.getHandler().handle(this, m.getMessage());
        }
    }
}
