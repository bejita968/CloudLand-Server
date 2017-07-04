package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.net.protocol.Entity;
import org.dragonet.cloudland.server.util.math.Vector3f;

/**
 * Created on 2017/6/11.
 */
public abstract class StandaloneEntity extends BaseEntity {

    @Override
    public boolean hasEntitySlots() {
        return false;
    }

    @Override
    public int getEntitySlots() {
        return 0;
    }

    @Override
    public Vector3f getEntitySlotRelativePosition(int index) {
        return null;
    }
}
