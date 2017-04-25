package org.dragonet.cloudland.server.network;

import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import com.google.protobuf.Message;
import io.netty.channel.socket.SocketChannel;

import java.net.SocketAddress;

/**
 * Created on 2016/11/27.
 */
public interface Session {

    CloudLandServer getServer();

    String getUniqueIdentifier();

    /**
     * Get the address for the remote client.
     * @return
     */
    SocketAddress getRemoteAddress();

    /**
     * Get associated socket channel.
     * @return
     */
    SocketChannel getChannel();

    NetworkServer getNetworkServer();

    /**
     * Determine the authentication status, usually done by checking whether the profile is null.
     * @return
     */
    boolean isAuthenticated();

    /**
     * MUST inplement this method for SessionManager to store the session.
     * @return
     */
    int hashCode();

    void sendNetworkMessage(Message message);

    /**
     * MAY BE NULL!!
     * @return
     */
    PlayerEntity getPlayer();

    /**
     * Can NOT set twice.
     * @param player
     */
    void setPlayer(PlayerEntity player);

    void disconnect(String reason);

    <T> T getOption(SessionOptions key, T def);

    void setOption(SessionOptions key, Object value);

    int getViewDistance();

    int getViewDistanceSquared();

    int getBlockViewDistanceSquared();

    void setViewDistance(int d);
}
