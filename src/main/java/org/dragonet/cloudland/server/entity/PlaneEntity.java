package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.net.protocol.*;
import org.dragonet.cloudland.net.protocol.Entity;
import org.dragonet.cloudland.server.util.Vector3D;
import org.dragonet.cloudland.server.util.math.Vector3f;

/**
 * Created on 2017/6/25.
 */
public class PlaneEntity extends BaseEntity {
    @Override
    public Vector3f getGatePosition(int gateIndex, boolean enter) {
        if(enter) {
            return new Vector3f(0.5f, 0.5f, 5f);
        } else {
            return new Vector3f(0.5f, 0.5f, 10f);
        }
    }

    /**
     * have slots?
     *
     * @return
     */
    @Override
    public boolean hasEntitySlots() {
        return false;
    }

    /**
     * how many slots?
     *
     * @return
     */
    @Override
    public int getEntitySlots() {
        return 0;
    }

    @Override
    public boolean enterable() {
        return true;
    }

    /**
     * where is that slot?
     *
     * @param index
     * @return
     */
    @Override
    public Vector3f getEntitySlotRelativePosition(int index) {
        return null;
    }

    /**
     * quit that slot, eg. stand up from a driver seat.
     *
     * @param action
     * @return where should this entity go? (described more in the ProtoBuf sources)
     */
    @Override
    public Vector3f quitSlot(Entity.ServerEntityHierarchicalControlMessage.HierarchicalAction action) {
        return null;
    }

    @Override
    public void spawnTo(PlayerEntity player) {
        Vector3D pos = getPosition();
        player.getSession().sendNetworkMessage(Entity.ServerAddEntityMessage.newBuilder()
                .setEntityId(getEntityId())
                .setEntityType(EntityType.toId("plane"))
                .setX(pos.x)
                .setY(pos.y)
                .setZ(pos.z)
                .setYaw(getYaw())
                .setPitch(getPitch())
                .setMeta(getMeta().serialize())
                .build());
    }
}
