package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;

/**
 * Created on 2017/1/23.
 */
public class DirtBehavior extends BlockBehavior {

    @Override
    public long getBreakTime(Item tool) {
        return 600L;
    }

    @Override
    public Item onPlace(PlayerEntity player, GameMap map, int x, int y, int z, Item tool) {
        return ItemPrototype.get("cloudland:dirt").newItemInstance(1);
    }

    @Override
    public Item[] getDrops(Item tool) {
        return new Item[]{ItemPrototype.get("cloudland:dirt").newItemInstance(1)};
    }
}
