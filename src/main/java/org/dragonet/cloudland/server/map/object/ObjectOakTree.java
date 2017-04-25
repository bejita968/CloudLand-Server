package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.item.Items;
import org.dragonet.cloudland.server.map.ChunkManager;
import org.dragonet.cloudland.server.util.NukkitRandom;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ObjectOakTree extends ObjectTree {
    private int treeHeight = 7;

    @Override
    public Items getTrunkBlock() {
        return Items.LOG;
    }

    @Override
    public Items getLeafBlock() {
        return Items.LEAVES;
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
    public void placeObject(ChunkManager map, int x, int y, int z, NukkitRandom random) {
        this.treeHeight = random.nextBoundedInt(3) + 4;
        super.placeObject(map, x, y, z, random);
    }
}
