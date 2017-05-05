package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.object.ObjectTree;
import org.dragonet.cloudland.server.util.NukkitRandom;
import org.dragonet.cloudland.server.util.math.NukkitMath;

/**
 * Created on 2017/2/26.
 */
public class TreePopulator implements Populator {

    private final static int DIRT_ID = ItemPrototype.toId("cloudland:dirt");
    private final static int GRASS_ID = ItemPrototype.toId("cloudland:grass");

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
            int b = this.level.getBlockAt(x, y, z);
            if (b == DIRT_ID || b == GRASS_ID) {
                break;
            } else if (b != 0){
                return -1;
            }
        }

        return ++y;
    }
}
