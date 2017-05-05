package org.dragonet.cloudland.server.map;

import org.dragonet.cloudland.net.protocol.Map;
import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.Entity;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.map.generator.Generator;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created on 2017/1/10.
 */
public class GameMap  {

    private AtomicLong entityIdCount = new AtomicLong();

    @Getter
    private final CloudLandServer server;

    private UnsignedLongKeyMap<Entity> entities = new UnsignedLongKeyMap<>(true);

    @Getter
    private final ChunkManager chunkManager;

    @Getter
    public final String name;
    @Getter
    public final Generator generator;

    @Getter
    private long seed;

    public GameMap(CloudLandServer server, String name, String generatorName, long seed) {
        this.server = server;
        this.chunkManager = new ChunkManager(this);
        this.name = name;
        this.seed = seed;
        Generator g = null;
        try {
            g = server.getGenerator(generatorName).getConstructor(new Class[]{GameMap.class, long.class}).newInstance(this, this.seed);
        }catch(Exception e){
            e.printStackTrace();
        }
        this.generator = g;
    }

    public long nextEntityId(){
        return entityIdCount.incrementAndGet();
    }

    public Entity getEntity(long entityId) {
        return entities.get(entityId);
    }

    public void addEntity(Entity entity) {
        if(entity == null) return;
        if(entity.getEntityId() <= 0) entity.setEntityId(nextEntityId());
        entities.put(entity.getEntityId(), entity);
        entity.setMap(this);
    }

    public void removeEntity(Entity entity) {
        if(entity == null || entity.getEntityId() <= 0) return;
        //We don't do that: entity.setMap(null);
        entity.setEntityId(-1); // It will automatically remove the chunk reference
        entities.remove(entity.getEntityId());
        if(PlayerEntity.class.isAssignableFrom(entity.getClass())) {
            chunkManager.unlockChunk((int)(entity.getPosition().getBlockX() >> 4), (int)(entity.getPosition().getBlockY() >> 4), (PlayerEntity) entity);
        }
    }

    public void forEachEntity(Consumer<Entity> e){
        entities.forEachValue(e);
    }

    public boolean setBlockAt(int x, int y, int z, int id, int meta) {
        LoadedChunk c = chunkManager.getChunk(x >> 4, z >> 4, true, false);
        if(c == null) {
            c = chunkManager.loadEmptyChunk(x >> 4, z >> 4);
        }
        int bx = x & 0xf;
        int bz = z & 0xf;
        if(c.getBlock(bx, y, bz) == id) return false;
        c.setBlock(bx, y, bz, id);
        c.forAllHolders((p) -> p.getSession().sendNetworkMessage(Map.ServerUpdateBlockMessage.newBuilder()
                .setX(x)
                .setY(y)
                .setZ(z)
                .setId(id)
                .build())
        );
        broadcastBlockUpdate(x, y, z); // function of GameMap
        return true;
    }

    public boolean removeBlockAt(int x, int y, int z){
        LoadedChunk c = chunkManager.getChunk(x >> 4, z >> 4, true, true);
        if(c.getBlock(x, y, z) == 0) return false;
        int bx = x & 0xf;
        int bz = z & 0xf;
        c.setBlock(bx, y, bz, 0);
        c.forAllHolders((p) -> p.getSession().sendNetworkMessage(Map.ServerUpdateBlockMessage.newBuilder()
                .setX(x)
                .setY(y)
                .setZ(z)
                .setId(0)
                .build())
        );
        broadcastBlockUpdate(x, y, z); // function of GameMap
        return true;
    }

    public int getBlockAt(int x, int y, int z) {
        LoadedChunk c = chunkManager.getChunk(x >> 4, z >> 4, false, false);
        if(c == null) return -1;
        return c.getBlock(x & 0xF, y ,z & 0xF);
    }

    public void setBlockAt(int x, int y, int z, int id) {
        setBlockAt(x, y, z, id, 0);
    }

    public void broadcastBlockUpdate(int x, int y, int z) {
        LoadedChunk c = chunkManager.getChunk(x >> 4, z >> 4, false, false);
        if(c == null) return;
        c.broadcastBlockUpdate(x & 0xF, y, z & 0xF); // Y MUST *NOT* USE AND OPERATION
    }

    public BinaryMetadata getRuntimeMeta(int x, int y, int z) {
        if(!chunkManager.isChunkLoaded(x >> 4, z >> 4)) {
            return null;
        }
        LoadedChunk chunk = chunkManager.getChunk(x >> 4, z >> 4, false, false);
        return chunk.getRuntimeMeta(x & 0xF, y, z & 0xF);
    }

    public void setRuntimeMeta(int x, int y, int z, BinaryMetadata meta) {
        if(!chunkManager.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        LoadedChunk chunk = chunkManager.getChunk(x >> 4, z >> 4, false, false);
        chunk.setRuntimeMeta(x & 0xF, y, z & 0xF, meta);
    }

    /**
     * TICK
     */
    public void tick() {
        chunkManager.tick();
        entities.forEachValue((e) -> e.tick());
    }
}
