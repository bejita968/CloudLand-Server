package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.item.Items;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.util.NukkitRandom;

/**
 * Created on 2017/2/26.
 */
public class GroundPopulator implements Populator {
    @Override
    public void populate(Chunk chunk, NukkitRandom random) {

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                // Biome biome = Biome.getBiome(chunk.getBiomeId(x, z));
                Items[] cover = new Items[]{Items.GRASS, Items.DIRT, Items.DIRT, Items.DIRT}; //biome.getGroundCover();
                if (cover != null && cover.length > 0) {
                    int diffY = 0;
                    if (!cover[0].getBlockBehavior().isSolid()) {
                        diffY = 1;
                    }

                    int y;
                    for (y = 127; y > 0; --y) {
                        int id = chunk.getBlockId(x, y, z);
                        int meta = chunk.getBlockMeta(x, y, z);

                        // Ignore trees
                        if(id == Items.LOG.getId() || id == Items.LEAVES.getId()) id = 0;

                        if (id != 0 && !BlockBehavior.get(id, meta).isTransparent()) {
                            break;
                        }
                    }
                    int startY = Math.min(127, y + diffY);
                    int endY = startY - cover.length;
                    for (y = startY; y > endY && y >= 0; --y) {
                        int id = chunk.getBlockId(x, y, z);
                        Items b = cover[startY - y];
                        if (id == 0x00 && b.getBlockBehavior().isSolid()) {
                            break;
                        }
                        chunk.setBlock(x, y, z, b.getId(), b.getMeta());
                    }
                }
            }
        }
    }

}
