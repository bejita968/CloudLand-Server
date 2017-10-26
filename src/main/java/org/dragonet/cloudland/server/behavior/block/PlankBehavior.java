package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;

/**
 * Created on 2017/5/19.
 */
public class PlankBehavior extends BlockBehavior {

    @Override
    public Item[] getDrops(Item tool) {
        return new Item[]{ItemPrototype.get("cloudland:plank").newItemInstance(1)};
    }

    @Override
    public long getBreakTime(Item tool) {
        if (tool != null) {
            String name = ItemPrototype.toName(tool.getId());
            if (name.endsWith("_axe")) { // hacky way
                if (name.equals("cloudland:wood_axe")) {
                    return 800L;
                }
            }
        }
        return 1500L;
    }

    @Override
    public Item onPlace(PlayerEntity player, GameMap map, int x, int y, int z, Item tool) {
        return ItemPrototype.get("cloudland:plank").newItemInstance(1);
    }
}
