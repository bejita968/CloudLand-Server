package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.Items;
import org.dragonet.cloudland.server.map.GameMap;

/**
 * Created on 2017/1/17.
 */
public class StoneBehavior extends BlockBehavior {

    private final static Item PLACING = Items.STONE.newItemInstance(0);

    @Override
    public long getBreakTime(Item tool) {
        return 10000L;
    }

    @Override
    public Item onPlace(PlayerEntity player, GameMap map, int x, int y, int z, Item tool) {
        return PLACING;
    }

    @Override
    public Item[] getDrops(Item tool) {
        return new Item[]{Items.STONE.newItemInstance(1)};
    }
}
