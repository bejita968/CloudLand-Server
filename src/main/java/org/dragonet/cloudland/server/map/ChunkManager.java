package org.dragonet.cloudland.server.map;

import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/1/10.
 */
public class ChunkManager {

    @Getter
    private final CloudLandServer server;

    private final GameMap map;

    private Map<Integer, Map<Integer, LoadedChunk>> chunks;

    public ChunkManager(GameMap map){
        this.map = map;
        this.server = map.getServer();
        chunks = Collections.synchronizedMap(new HashMap<>());
    }

    public LoadedChunk getChunk(int x, int z, boolean generate, boolean populate) {
        if(!chunks.containsKey(x)) return loadChunk(x, z, generate, populate);
        Map<Integer, LoadedChunk> zMap = chunks.get(x);
        if(!zMap.containsKey(z)) return loadChunk(x, z, generate, populate);
        LoadedChunk c = chunks.get(x).get(z);
        if(!c.isGenerated()) {
            if(!generate) return null;
            map.generator.generate(c, populate);
        }
        if(!c.isPopulated() && populate) {
            map.generator.generate(c, true);
        }
        return chunks.get(x).get(z);
    }

    public boolean isChunkLoaded(int x, int z){
        if(chunks.containsKey(x) && chunks.get(x).containsKey(z)) {
            return true;
        } else {
            return false;
        }
    }

    public LoadedChunk loadChunk(int x, int z, boolean generate) {
        return loadChunk(x, z, generate, true);
    }

    public LoadedChunk loadChunk(int x, int z, boolean generate, boolean populate) {
        if(chunks.containsKey(x) && chunks.get(x).containsKey(z)){
            LoadedChunk c = chunks.get(x).get(z);
            if(!c.isGenerated()) {
                if(!generate) return null;
                map.generator.generate(c, populate);
            }
            if(!c.isPopulated() && populate) {
                map.generator.generate(c, true);
            }
            return c;
        }
        if(!generate) return null;
        //TODO: Try load from disk
        //...
        //Generate
        LoadedChunk c = new LoadedChunk(map, x, z);
        if(!chunks.containsKey(x)) chunks.put(x, Collections.synchronizedMap(new HashMap<>()));
        chunks.get(x).put(z, c);

        map.generator.generate(c, populate);
        return c;
    }

    public void lockChunk(int x, int z, PlayerEntity holder) {
        LoadedChunk c = getChunk(x, z, false, false);
        //server.getLogger().info("[#] Locking chunk at (" + x + ", " + z + "), null? " + (c == null));
        if(c == null) return;
        c.lock(holder);
    }

    public void unlockChunk(int x, int z, PlayerEntity holder) {
        LoadedChunk c = getChunk(x, z, false, false);
        //server.getLogger().info("[-] Unlocking chunk at (" + x + ", " + z + "), null? " + (c == null));
        if(c == null) return;
        c.unlock(holder);
    }

    public LoadedChunk loadEmptyChunk(int x, int z) {
        LoadedChunk c = new LoadedChunk(map, x, z);
        if(!chunks.containsKey(x)) chunks.put(x, Collections.synchronizedMap(new HashMap<>()));
        chunks.get(x).put(z, c);
        return c;
    }

    /**
     * THIS FUNCTION WILL NOT UPDATE CLIENTS, CALL FROM GAMEMAP ONLY!!
     * @param x
     * @param y
     * @param z
     * @param id
     * @param meta
     * @return
     */
    public boolean setBlockAt(int x, int y, int z, int id, int meta) {
        LoadedChunk c = getChunk(x >> 4, z >> 4, true, false);
        int bx = x & 0xf;
        int bz = z & 0xf;
        if(c.getBlockId(bx, y, bz) == id && c.getBlockMeta(bx, y, bz) == meta) return false;
        c.setBlockId(bx, y, bz, id);
        c.setBlockMeta(bx, y, bz, meta);
        return true;
    }

    public int getBlockIdAt(int x, int y, int z) {
        LoadedChunk c = getChunk(x >> 4, z >> 4, false, false);
        if(c == null) return 0;
        return c.getBlockId(x & 0xF, y ,z & 0xF);
    }

    public int getBlockMetaAt(int x, int y, int z) {
        LoadedChunk c = getChunk(x >> 4, z >> 4, false, false);
        if(c == null) return 0;
        return c.getBlockMeta(x & 0xF, y ,z & 0xF);
    }

    public void setBlockIdAt(int x, int y, int z, int id) {
        setBlockAt(x, y, z, id, 0);
    }

    public void tick(){
        chunks.values().forEach((v) -> {
            v.values().forEach((c) -> {
                // Tick chunk
                c.tick();
            });
        });
    }
}
