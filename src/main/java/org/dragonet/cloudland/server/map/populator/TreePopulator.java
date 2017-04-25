package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.item.Items;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.object.ObjectTree;
import org.dragonet.cloudland.server.util.NukkitRandom;
import org.dragonet.cloudland.server.util.math.NukkitMath;

/**
 * Created on 2017/2/26.
 */
public class TreePopulator implements Populator {

    private final GameMap level;
    private int randomAmount;
    private int baseAmount;

    private final int type;

    public TreePopulator(GameMap level, int type) {
        this.level = level;
        this.type = type;
    }

    public TreePopulator(GameMap level, int type, int baseAmount) {
        this.level = level;
        this.type = type;
        this.baseAmount = baseAmount;
    }

    public void setRandomAmount(int randomAmount) {
        this.randomAmount = randomAmount;
    }

    public void setBaseAmount(int baseAmount) {
        this.baseAmount = baseAmount;
    }

    @Override
    public void populate(Chunk chunk, NukkitRandom random) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        int amount = random.nextBoundedInt(this.randomAmount + 1) + this.baseAmount;
        for (int i = 0; i < amount; ++i) {
            int x = NukkitMath.randomRange(random, chunkX << 4, (chunkX << 4) + 15);
            int z = NukkitMath.randomRange(random, chunkZ << 4, (chunkZ << 4) + 15);
            int y = this.getHighestWorkableBlock(x, z);
            if (y == -1) {
                continue;
            }
            ObjectTree.growTree(level.getChunkManager(), x, y, z, random, this.type);
        }
    }

    private int getHighestWorkableBlock(int x, int z) {
        int y;
        for (y = 127; y > 0; --y) {
            int b = this.level.getBlockIdAt(x, y, z);
            if (b == Items.DIRT.getId() || b == Items.GRASS.getId()) {
                break;
            } else if (b != Items.AIR.getId()){
                return -1;
            }
        }

        return ++y;
    }
}
