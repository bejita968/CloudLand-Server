package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.ChunkManager;
import org.dragonet.cloudland.server.util.NukkitRandom;

import java.util.Random;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ObjectOakTree extends ObjectTree {

    private final static ItemPrototype LOG = ItemPrototype.get("cloudland:log");
    private final static ItemPrototype LEAVES = ItemPrototype.get("cloudland:leaves");

    private int treeHeight = 7;

    @Override
    public ItemPrototype getTrunkBlock() {
        return LOG;
    }

    @Override
    public ItemPrototype getLeafBlock() {
        return LEAVES;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getTreeHeight() {
        return this.treeHeight;
    }

    @Override
    public void placeObject(ChunkManager map, int x, int y, int z, Random random) {
        this.treeHeight = random.nextInt(3) + 4;
        super.placeObject(map, x, y, z, random);
    }
}
