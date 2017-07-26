package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.object.OreType;
import org.dragonet.cloudland.server.map.object.OreVein;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created on 2017/7/26.
 */
public class OrePopulator implements Populator {

    private final Map<OreType, Integer> ores = new LinkedHashMap<>();

    public OrePopulator(){
        ores.put(new OreType(ItemPrototype.toId("cloudland:dirt"), 0, 96, 16, ItemPrototype.toId("cloudland:dirt")), 5);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        int cx = chunk.getX() << 4;
        int cz = chunk.getZ() << 4;

        for (Map.Entry<OreType, Integer> entry : ores.entrySet()) {

            OreType oreType = entry.getKey();
            for (int n = 0; n < entry.getValue(); n++) {
                int sourceX = cx + random.nextInt(16);
                int sourceZ = cz + random.nextInt(16);
                int sourceY = oreType.getMinY() == oreType.getMaxY() ?
                        random.nextInt(oreType.getMinY()) + random.nextInt(oreType.getMinY()) :
                        random.nextInt(oreType.getMaxY() - oreType.getMinY()) + oreType.getMinY();

                new OreVein(oreType).generate(chunk.getMap(), random, sourceX, sourceY, sourceZ);
            }
        }
    }
}
