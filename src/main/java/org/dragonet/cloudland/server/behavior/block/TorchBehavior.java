package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;

/**
 * Created on 2017/5/19.
 */
public class TorchBehavior extends BlockBehavior {

    @Override
    public Item onPlace(PlayerEntity player, GameMap map, int x, int y, int z, Item tool) {
        return ItemPrototype.get("cloudland:torch").newItemInstance(1);
    }

    @Override
    public long getBreakTime(Item tool) {
        return 200L;
    }

    @Override
    public Item[] getDrops(Item tool) {
        // we get creative now, it drops one stick and one plank
        return new Item[] {
                ItemPrototype.get("cloudland:plank").newItemInstance(1),
                ItemPrototype.get("cloudland:wood_stick").newItemInstance(1)
        };
    }
}
