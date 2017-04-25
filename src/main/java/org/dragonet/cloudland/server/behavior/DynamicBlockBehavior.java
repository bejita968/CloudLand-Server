package org.dragonet.cloudland.server.behavior;

import org.dragonet.cloudland.server.map.GameMap;

/**
 * Created on 2017/4/23.
 */
public abstract class DynamicBlockBehavior extends BlockBehavior {
    /**
     *
     * @param map
     * @param x
     * @param y
     * @param z
     * @return should this block still be updated
     */
    public abstract boolean onUpdate(GameMap map, int x, int y, int z);
}
