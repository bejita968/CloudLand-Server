package org.dragonet.cloudland.server.map.generator;

import org.dragonet.cloudland.server.map.chunk.Chunk;

/**
 * Created on 2017/1/10.
 */
public interface Generator {

    Chunk generate(Chunk chunk, boolean populate);

}
