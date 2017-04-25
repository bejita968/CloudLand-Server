package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.map.GameMap;

/**
 * Created on 2017/2/26.
 */
public class WaterBehavior extends BlockBehavior {
    @Override
    public Item[] getDrops(Item tool) {
        return null;
    }

    @Override
    public boolean onStartBreak(PlayerEntity player, GameMap map, int x, int y, int z, Item tool) {
        return false;
    }

    @Override
    public boolean onEndBreak(PlayerEntity player, GameMap map, int x, int y, int z, Item tool, long breakTime) {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
