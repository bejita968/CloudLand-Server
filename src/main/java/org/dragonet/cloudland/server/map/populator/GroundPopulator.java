package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.chunk.Chunk;

import java.util.Random;

/**
 * Created on 2017/2/26.
 */
public class GroundPopulator implements Populator {

    private final static int LOG_ID = ItemPrototype.toId("cloudland:log");
    private final static int LEAVES_ID = ItemPrototype.toId("cloudland:leaves");
    private final static ItemPrototype GRASS = ItemPrototype.get("cloudland:grass");
    private final static ItemPrototype DIRT = ItemPrototype.get("cloudland:dirt");
    private final static ItemPrototype[] cover = new ItemPrototype[]{
            GRASS,
            DIRT,
            DIRT,
            DIRT};

    @Override
    public void populate(Chunk chunk, Random random) {

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                if (cover != null && cover.length > 0) {
                    int diffY = 0;
                    if (!cover[0].getBlockBehavior().isSolid()) {
                        diffY = 1;
                    }

                    int y;
                    for (y = 127; y > 0; --y) {
                        int id = chunk.getBlock(x, y, z);

                        // Ignore trees
                        if(id == LOG_ID || id == LEAVES_ID) id = 0;

                        if (id != 0 && !BlockBehavior.get(id).isTransparent()) {
                            break;
                        }
                    }
                    int startY = Math.min(127, y + diffY);
                    int endY = startY - cover.length;
                    for (y = startY; y > endY && y >= 0; --y) {
                        int id = chunk.getBlock(x, y, z);
                        ItemPrototype b = cover[startY - y];
                        if (id == 0x00 && b.getBlockBehavior().isSolid()) {
                            break;
                        }
                        chunk.setBlock(x, y, z, b.getId());
                    }
                }
            }
        }
    }

}
