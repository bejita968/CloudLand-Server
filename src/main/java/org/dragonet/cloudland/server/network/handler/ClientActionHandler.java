package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Movement;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/1/18.
 */
public class ClientActionHandler implements CLMessageHandler<Movement.ClientActionMessage> {
    @Override
    public void handle(Session session, Movement.ClientActionMessage message) {
        if(!session.isAuthenticated()) return;
        Movement.ClientActionMessage.ActionType type = message.getAction();
        switch(type) {
            case START_BREAK:
                session.getPlayer().startBreaking(message.getBlockX(), message.getBlockY(), message.getBlockZ());
                break;
            case CANCEL_BREAK:
                session.getPlayer().cancelBreaking();
                break;
        }
    }
}
