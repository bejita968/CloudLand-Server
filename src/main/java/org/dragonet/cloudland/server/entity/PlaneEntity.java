package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.net.protocol.*;
import org.dragonet.cloudland.net.protocol.Entity;
import org.dragonet.cloudland.server.util.Vector3D;
import org.dragonet.cloudland.server.util.math.Vector3f;

/**
 * Created on 2017/6/25.
 */
public class PlaneEntity extends BaseEntity {

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
