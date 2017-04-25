package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Authentication;
import org.dragonet.cloudland.net.protocol.Initial;
import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;
import org.dragonet.cloudland.server.util.PlayerProfile;

import java.util.UUID;

/**
 * Created on 2017/1/9.
 */
public class ClientAuthenticateHandler implements CLMessageHandler<Authentication.ClientAuthenticateMessage> {
    @Override
    public void handle(Session session, Authentication.ClientAuthenticateMessage message) {
        if(session.isAuthenticated()) {
            session.disconnect("protocol error: wrong state");
            return;
        }

        // TODO: Process authentication
        // ...

        session.sendNetworkMessage(Authentication.ServerAuthenticateResultMessage.newBuilder()
                .setResult(Authentication.ServerAuthenticateResultMessage.LoginResult.SUCCESS)
                .build());

        String username = "test_user_" + System.currentTimeMillis();

        try {
            PlayerProfile profile = new PlayerProfile(0L, username);
            PlayerEntity entity = new PlayerEntity(session, profile);
            session.setPlayer(entity);
            session.getServer().getMap(CloudLandServer.DEFAULT_MAP_NAME).addEntity(entity);

            session.sendNetworkMessage(Initial.ServerJoinGameMessage.newBuilder()
                    .setUsername(username)
                    .setUuid(UUID.randomUUID().toString())
                    .setWorldId(0)
                    .setX(0f)
                    .setY(220.0f)
                    .setZ(0f)
                    .build());
        }catch(Exception e){
            e.printStackTrace();
        }
        session.getServer().getLogger().info("Player [" + username + "] is joining the server! ");
    }
}
