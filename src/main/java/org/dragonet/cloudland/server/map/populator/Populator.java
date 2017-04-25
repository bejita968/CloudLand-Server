package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.util.NukkitRandom;

/**
 * Created on 2017/2/26.
 */
public interface Populator {

    void populate(Chunk chunk, NukkitRandom random);

}
