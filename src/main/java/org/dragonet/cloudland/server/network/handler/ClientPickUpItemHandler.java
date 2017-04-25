package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Inventory;
import org.dragonet.cloudland.server.entity.Entity;
import org.dragonet.cloudland.server.entity.ItemEntity;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;

/**
 * Created on 2017/1/20.
 */
public class ClientPickUpItemHandler implements CLMessageHandler<Inventory.ClientPickUpItemMessage> {
    @Override
    public void handle(Session session, Inventory.ClientPickUpItemMessage message) {
        if(!session.isAuthenticated()) return;
        Entity entity = session.getPlayer().getMap().getEntity(message.getEntityId());
        // System.out.println("[ITEM PICKUP] entity == null ? " + (entity == null));
        if(entity == null || !ItemEntity.class.isAssignableFrom(entity.getClass())) {
            return;
        }
        ItemEntity itemEntity = (ItemEntity) entity;
        // System.out.println("[ITEM PICKUP] distance: " + Math.sqrt(itemEntity.getPosition().getDistanceSquared(session.getPlayer().getPosition())));
        if(itemEntity.getPosition().getDistanceSquared(session.getPlayer().getPosition()) > 16) {
            return;
        }
        // System.out.println("[ITEM PICKUP] Picking up " + message.getEntityId());
        int left = itemEntity.getItem().getCount() - session.getPlayer().getInventory().addItem(itemEntity.getItem());
        // System.out.println("[ITEM PICKUP] left: " + left);
        if(left > 0) {
            itemEntity.getItem().setCount(left);
            itemEntity.markMetaChanged();
        } else {
            session.getPlayer().getMap().removeEntity(itemEntity);
        }
    }
}
