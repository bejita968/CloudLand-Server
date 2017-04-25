package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Map;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/1/18.
 */
public class ClientRemoveBlockHandler implements CLMessageHandler<Map.ClientRemoveBlockMessage> {
    @Override
    public void handle(Session session, Map.ClientRemoveBlockMessage message) {
        if(!session.isAuthenticated()) return;
        session.getPlayer().endBreaking(message.getX(), message.getY(), message.getZ());
    }
}
