package org.dragonet.cloudland.server.behavior.block;

import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.gui.window.CraftingWindow;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.Direction;

/**
 * Created on 2017/10/26.
 */
public class CraftingTableBehavior extends PlankBehavior {

    @Override
    public Item[] getDrops(Item tool) {
        return new Item[] {ItemPrototype.get("cloudland:crafting_table").newItemInstance(1)};
    }

    @Override
    public boolean onTouch(PlayerEntity player, GameMap map, int x, int y, int z, Direction direction, Item tool) {
        CraftingWindow crafting = new CraftingWindow("Crafting");
        crafting.openTo(player);
        return false;
    }

    @Override
    public Item onPlace(PlayerEntity player, GameMap map, int x, int y, int z, Item tool) {
        return ItemPrototype.get("cloudland:crafting_table").newItemInstance(1);
    }
}
