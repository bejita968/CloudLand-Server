package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Handshake;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/1/9.
 */
public class ClientHandshakeHandler implements CLMessageHandler<Handshake.ClientHandshakeMessage> {
    @Override
    public void handle(Session session, Handshake.ClientHandshakeMessage message) {
        int vd = message.getRenderDistance();
        if(vd < 6 || vd > 16) vd = 6;
        session.setViewDistance(vd);

        session.sendNetworkMessage(Handshake.ServerHandshakeMessage.newBuilder()
        .setSuccess(true)
        .setStatus(Handshake.ServerHandshakeMessage.ServerStatus.NORMAL)
        .build());
    }
}
