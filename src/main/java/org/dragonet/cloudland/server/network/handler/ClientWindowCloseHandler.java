package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/3/15.
 */
public class ClientWindowCloseHandler implements CLMessageHandler<GUI.ClientWindowCloseMessage> {
    @Override
    public void handle(Session session, GUI.ClientWindowCloseMessage message) {
        if(!session.isAuthenticated()) return;
        session.getPlayer().closeWindow(message.getWindowId(), true);
    }
}
