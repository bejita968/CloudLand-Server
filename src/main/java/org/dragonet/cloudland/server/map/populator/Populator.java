package org.dragonet.cloudland.server.map.populator;

import org.dragonet.cloudland.server.map.chunk.Chunk;

import java.util.Random;

/**
 * Created on 2017/2/26.
 */
public interface Populator {

    void populate(Chunk chunk, Random random);

}
