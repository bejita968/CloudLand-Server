package org.dragonet.cloudland.server.network.handler;

import org.dragonet.cloudland.net.protocol.Movement;
import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.protocol.CLMessageHandler;
import org.dragonet.cloudland.server.util.Direction;

/**
 * Created on 2017/1/24.
 */
public class ClientUseItemHandler implements CLMessageHandler<Movement.ClientUseItemMessage> {
    @Override
    public void handle(Session session, Movement.ClientUseItemMessage message) {
        if(!session.isAuthenticated()) return;
        if(message.getHotbarIndex() != session.getPlayer().getInventory().getSelectedSlot()) {
            session.getPlayer().getInventory().setSelectedSlot(session.getPlayer().getInventory().getSelectedSlot());
            return;
        }
        Item holding = session.getPlayer().getInventory().getHoldingItem();
        // System.out.println("MAP == NULL ? " + (session.getPlayer().getMap() == null));
        int blockId = session.getPlayer().getMap().getBlockAt(message.getBlockX(), message.getBlockY(), message.getBlockZ());
        Direction dir = Direction.values()[message.getDirection()];
        BlockBehavior behavior = BlockBehavior.get(blockId);
        if(behavior != null){
            boolean touch = behavior.onTouch(session.getPlayer(), session.getPlayer().getMap(), message.getBlockX(), message.getBlockY(), message.getBlockZ(), dir, holding);
            if(!touch) return;
        }
        if(holding != null){
            // Use a item to touch a block
            BlockBehavior placing = BlockBehavior.get(holding.getId());
            if(placing != null) {
                // Place a block
                int[] appliedPosition = dir.add(message.getBlockX(), message.getBlockY(), message.getBlockZ());
                Item placingResult = placing.onPlace(session.getPlayer(), session.getPlayer().getMap(), appliedPosition[0], appliedPosition[1], appliedPosition[2], holding);
                if(placingResult != null) {
                    session.getPlayer().getMap().setBlockAt(appliedPosition[0], appliedPosition[1], appliedPosition[2], placingResult.getId());
                    holding.setCount(holding.getCount() - 1);
                    if(holding.getCount() <= 0) {
                        session.getPlayer().getInventory().setSlot(session.getPlayer().getInventory().getSelectedSlot(), null);
                    }
                    // Client already updated so we do nothing
                } else {
                    // Revert the change
                    session.getPlayer().getMap().broadcastBlockUpdate(message.getBlockX(), message.getBlockY(), message.getBlockZ());
                    session.getPlayer().getMap().broadcastBlockUpdate(appliedPosition[0], appliedPosition[1], appliedPosition[2]);
                    session.getPlayer().getInventory().sendContents();
                }
            }
        }
    }
}
