package org.dragonet.cloudland.server.map.generator;

import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.populator.Populator;

import java.util.List;

/**
 * Created on 2017/1/10.
 */
public interface Generator {

    Chunk generate(Chunk chunk);

    List<Populator> getPopulators(int x, int z);

}
