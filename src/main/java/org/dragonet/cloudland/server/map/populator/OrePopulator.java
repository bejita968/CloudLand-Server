package org.dragonet.cloudland.server.map.populator;

import lombok.Getter;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.object.OreType;
import org.dragonet.cloudland.server.map.object.OreVein;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Populates the world with ores.
 */
public class OrePopulator implements Populator {

    private final GameMap map;
    @Getter
    private final Map<OreType, Integer> ores = new LinkedHashMap<>();

    public OrePopulator(GameMap map) {
        this.map = map;
    }

    /*
    // saved for further reference
    private void init() {
        ores.put(new OreType(Material.GRAVEL, 0, 256, 32), 8);
        ores.put(new OreType(Material.STONE, 0, 80, 32), 10);
        ores.put(new OreType(Material.STONE, 0, 80, 32), 10);
        ores.put(new OreType(Material.STONE, 0, 80, 32), 10);
        ores.put(new OreType(Material.COAL_ORE, 0, 128, 16), 20);
        ores.put(new OreType(Material.IRON_ORE, 0, 64, 8), 20);
        ores.put(new OreType(Material.GOLD_ORE, 0, 32, 8), 2);
        ores.put(new OreType(Material.REDSTONE_ORE, 0, 16, 7), 8);
        ores.put(new OreType(Material.DIAMOND_ORE, 0, 16, 7), 1);
        ores.put(new OreType(Material.LAPIS_ORE, 16, 16, 6), 1);
    }
    */

    @Override
    public void populate(Chunk chunk, Random random) {
        int cx = chunk.getX() << 4;
        int cz = chunk.getZ() << 4;

        for (Entry<OreType, Integer> entry : ores.entrySet()) {

            OreType oreType = entry.getKey();
            for (int n = 0; n < entry.getValue(); n++) {

                int sourceX = cx + random.nextInt(16);
                int sourceZ = cz + random.nextInt(16);
                int sourceY = oreType.getMinY() == oreType.getMaxY() ?
                        random.nextInt(oreType.getMinY()) + random.nextInt(oreType.getMinY()) :
                        random.nextInt(oreType.getMaxY() - oreType.getMinY()) + oreType.getMinY();

                new OreVein(oreType).generate(map, random, sourceX, sourceY, sourceZ);
            }
        }
    }
}
