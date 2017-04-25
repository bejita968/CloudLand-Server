package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Movement;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;
import org.dragonet.cloudland.server.util.Vector3D;

/**
 * Created on 2017/1/12.
 */
public class ClientMovementHandler implements CLMessageHandler<Movement.ClientMovementMessage> {
    @Override
    public void handle(Session session, Movement.ClientMovementMessage message) {
        if(!session.isAuthenticated()) {
            session.disconnect("moved before logging in");
            return;
        }
        //System.out.println("ClientMovementMessage: " + String.format("(%.2f, %.2f, %.2f) # %.2f, %.2f", message.getX(), message.getY(), message.getZ(), message.getYaw(), message.getPitch()));

        session.getPlayer().setRawPosition(new Vector3D(message.getX(),
                message.getY(),
                message.getZ()));

        session.getPlayer().setRotation(message.getYaw(), message.getPitch());
    }
}
