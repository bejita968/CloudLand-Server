package org.dragonet.cloudland.server.map;

import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import lombok.Getter;
import org.dragonet.cloudland.server.map.populator.Populator;
import org.dragonet.cloudland.server.util.NukkitRandom;

import java.util.*;

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

    public LoadedChunk getChunk(int x, int z, boolean generate) {
        if(!chunks.containsKey(x)) return loadChunk(x, z, generate);
        Map<Integer, LoadedChunk> zMap = chunks.get(x);
        if(!zMap.containsKey(z)) return loadChunk(x, z, generate);
        LoadedChunk c = chunks.get(x).get(z);
        if(!c.isGenerated() && generate) {
            map.generator.generate(c);
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
        if(chunks.containsKey(x) && chunks.get(x).containsKey(z)){
            LoadedChunk c = chunks.get(x).get(z);
            if(!c.isGenerated() && generate) {
                map.generator.generate(c);
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

        map.generator.generate(c);
        return c;
    }

    public void lockChunk(int x, int z, PlayerEntity holder) {
        LoadedChunk c = getChunk(x, z, false);
        //server.getLogger().info("[#] Locking chunk at (" + x + ", " + z + "), null? " + (c == null));
        if(c == null) return;
        c.lock(holder);
    }

    public void unlockChunk(int x, int z, PlayerEntity holder) {
        LoadedChunk c = getChunk(x, z, false);
        //server.getLogger().info("[-] Unlocking chunk at (" + x + ", " + z + "), null? " + (c == null));
        if(c == null) return;
        c.unlock(holder);
    }

    /**
     * THIS FUNCTION WILL NOT UPDATE CLIENTS, CALL FROM GAMEMAP ONLY!!
     * @param x
     * @param y
     * @param z
     * @param id
     * @return
     */
    public boolean setBlockAt(int x, int y, int z, int id) {
        LoadedChunk c = getChunk(x >> 4, z >> 4, true);
        int bx = x & 0xf;
        int bz = z & 0xf;
        if(c.getBlock(bx, y, bz) == id) return false;
        c.setBlock(bx, y, bz, id);
        return true;
    }

    public int getBlockAt(int x, int y, int z) {
        LoadedChunk c = getChunk(x >> 4, z >> 4, false);
        if(c == null) return 0;
        return c.getBlock(x & 0xF, y ,z & 0xF);
    }

    public void tick(){
        chunks.values().forEach((v) -> {
            v.values().forEach((c) -> {
                // Tick chunk
                c.tick();
            });
        });
    }

    public LoadedChunk populateChunk(int x, int z) {
        LoadedChunk c = getChunk(x, z, true);
        if(c == null) return null;
        if(c.isPopulated()) {
            return c;
        }

        // cancel out if the 3x3 around it isn't available
        for (int x2 = x - 1; x2 <= x + 1; ++x2) {
            for (int z2 = z - 1; z2 <= z + 1; ++z2) {
                if (!isChunkLoaded(x2, z2) && loadChunk(x2, z2, true) == null) {
                    return null;
                }
            }
        }

        // check again
        if(c.isPopulated()) {
            return c;
        }
        System.out.println(String.format("populating chunk XZ at [%d, %d]", x, z));
        List<Populator> populators = map.getGenerator().getPopulators(x, z);
        Random random = new Random(map.getSeed());
        long xRand = random.nextLong() / 2 * 2 + 1;
        long zRand = random.nextLong() / 2 * 2 + 1;
        long populatorSeed = x * xRand + z * zRand ^ map.getSeed();
        for(Populator p : populators) {
            try {
                p.populate(c, new Random(populatorSeed));
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("Error whilst using <" + p.getClass().getSimpleName() + "> to populate the chunk: " + e.getMessage());
            }
        }
        c.markPopulated();

        return c;
    }
}
