package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Inventory;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/1/20.
 */
public class ClientHotbarSelectionHandler implements CLMessageHandler<Inventory.ClientHotbarSelectionMessage> {
    @Override
    public void handle(Session session, Inventory.ClientHotbarSelectionMessage message) {
        if(!session.isAuthenticated()) return;

        session.getPlayer().setRawHoldingSlot(message.getIndex() % 9);
    }
}
