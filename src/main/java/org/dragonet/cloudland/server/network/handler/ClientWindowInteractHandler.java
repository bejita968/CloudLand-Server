package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.gui.GUIWindow;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/3/17.
 */
public class ClientWindowInteractHandler implements CLMessageHandler<GUI.ClientWindowInteractMessage> {
    @Override
    public void handle(Session session, GUI.ClientWindowInteractMessage message) {
        // System.out.println("INTERACTING WINDOW #" + message.getWindowId() + " AT ELEMENT #" + message.getElementIndex() + " BY " + message.getAction().toString() + ", PARAMS = " + message.getParam1() + "/" + message.getParam2());
        if(!session.isAuthenticated()) return;
        if(!session.getPlayer().isWindowOpened(message.getWindowId())){
            session.getPlayer().closeWindow(message.getWindowId(), false);
            return;
        }
        GUIWindow window = session.getPlayer().getWindow(message.getWindowId());
        window.onAction(session.getPlayer(), message.getElementIndex(), message.getAction(), message.getParam1(), message.getParam2());
    }
}
