package org.dragonet.cloudland.server.map;

import org.dragonet.cloudland.net.protocol.Map;
import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.behavior.DynamicBlockBehavior;
import org.dragonet.cloudland.server.entity.Entity;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.chunk.ChunkSection;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;
import org.dragonet.cloudland.server.util.math.Vector3;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created on 2017/1/10.
 */
public class LoadedChunk extends Chunk {

    @Getter
    private final GameMap map;

    private Set<PlayerEntity> holders = Collections.synchronizedSet(new HashSet<>());

    @Getter
    private final UnsignedLongKeyMap<Entity> entities = new UnsignedLongKeyMap<>(true);

    public final BinaryMetadata[][][] runtimeMeta = new BinaryMetadata[16][512][16];

    public LoadedChunk(GameMap map, int x, int z) {
        super(x, z);
        this.map = map;
    }

    public LoadedChunk(GameMap map, int x, int z, ChunkSection[] sections){
        super(x, z, sections);
        this.map = map;
    }

    public void lock(PlayerEntity holder) {
        if(holders.contains(holder)) return;
        holders.add(holder);
    }

    public void unlock(PlayerEntity holder){
        if(!holders.contains(holder)) return;
        holders.remove(holder);
    }

    public void updateEntityReference(Entity e){
        if(e.getPosition() == null) {
            entities.remove(e.getEntityId());
            return;
        }
        int cx = (int)(e.getPosition().getBlockX() >> 4);
        int cz = (int)(e.getPosition().getBlockZ() >> 4);
        if(map != e.getMap() || cx != getX() || cz != getZ()) {
            entities.remove(e.getEntityId());
            //if (x == 0 && z == 0) System.out.println("[EREF] C(" + x + ", " + z +  ") De-referencing entity " + e.getEntityId());
        } else {
            if(entities.containsKey(e.getEntityId())) return;
            entities.put(e.getEntityId(), e);
            //if (x == 0 && z == 0) System.out.println("[EREF] C(" + x + ", " + z +  ") Referencing entity " + e.getEntityId());
        }
    }

    public void removeEntityReference(Entity e){
        entities.remove(e.getEntityId());
    }

    public void broadcastUpdate() {
        holders.forEach((p) -> p.getChunkTask().queue(this));
    }

    public void broadcastBlockUpdate(int x, int y, int z) {
        forAllHolders((p) -> p.getSession().sendNetworkMessage(Map.ServerUpdateBlockMessage.newBuilder()
                        .setX((getX() << 4) + x)
                        .setY(y)
                        .setZ((getZ() << 4) + z)
                        .setId(getBlock(x & 0xF, y, z & 0xF))
                        .build()));
    }

    public void tick() {
        Set<Vector3> dyn = getDynamicBlocks();
        ArrayList<Vector3> toRemove = null;
        if(dyn != null) {
            for(Vector3 pos : dyn) {
                int localX = pos.getFloorX();
                int localY = pos.getFloorY();
                int localZ = pos.getFloorZ();
                int id = getBlock(localX, localY, localZ);
                BlockBehavior behavior = BlockBehavior.get(id);
                if(behavior == null) {
                    if(toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(pos);
                }
                if(!DynamicBlockBehavior.class.isAssignableFrom(behavior.getClass())) {
                    if(toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(pos);
                }
                int x = getX() << 4 + localX;
                int z = getZ() << 4 + localZ;
                boolean ret = ((DynamicBlockBehavior)behavior).onUpdate(getMap(), x, localY, z);
                if(!ret) {
                    if(toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(pos);
                }
            }
        }
    }

    public void forAllHolders(Consumer<PlayerEntity> consumer) {
        holders.forEach(consumer);
    }

    public BinaryMetadata getRuntimeMeta(int x, int y, int z) {
        if(runtimeMeta[x][y][z] == null) {
            return new BinaryMetadata();
        }
        return runtimeMeta[x][y][z];
    }

    public void setRuntimeMeta(int x, int y, int z, BinaryMetadata meta) {
        runtimeMeta[x][y][z] = meta;
    }
}
