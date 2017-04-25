package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.Items;

/**
 * Created on 2017/2/26.
 */
public class SandBehavior extends BlockBehavior {

    @Override
    public long getBreakTime(Item tool) {
        return 200;
    }

    @Override
    public Item[] getDrops(Item tool) {
        return new Item[]{Items.SAND.newItemInstance(1)};
    }
}
