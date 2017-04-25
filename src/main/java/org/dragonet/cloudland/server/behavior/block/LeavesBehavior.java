package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.item.Item;

/**
 * Created on 2017/2/26.
 */
public class LeavesBehavior extends BlockBehavior {

    @Override
    public Item[] getDrops(Item tool) {
        return null;
    }

    @Override
    public long getBreakTime(Item tool) {
        return 200;
    }
}
