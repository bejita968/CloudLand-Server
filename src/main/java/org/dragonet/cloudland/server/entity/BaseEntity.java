package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.net.protocol.Metadata;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.util.Vector3D;
import lombok.Getter;

/**
 * Created on 2017/1/10.
 */
public abstract class BaseEntity implements Entity {

    @Getter
    private long entityId = -1;

    @Getter
    private Vector3D position = new Vector3D();

    @Getter
    private GameMap map;

    @Getter
    private BinaryMetadata meta = new BinaryMetadata();

    @Getter
    private float yaw;

    @Getter
    private float pitch;

    private boolean positionChange = false;
    private boolean rotationChange = false;
    private boolean metaChange = false;

    @Override
    public void setEntityId(long entityId) {
        if(this.entityId != -1){
            if(entityId == -1){
                // This entity is removed
                getChunk().removeEntityReference(this);
                this.map = null;
                return;
            }
            throw new IllegalStateException("entity id already set! ");
        }
        this.entityId = entityId;
    }

    @Override
    public LoadedChunk getChunk(){
        return getMap().getChunkManager().getChunk((int)(getPosition().getBlockX() >> 4), (int)(getPosition().getBlockZ() >> 4), true, false);
    }

    @Override
    public void setPosition(Vector3D position) {
        //System.out.println("BaseEntity: Setting position to " + String.format("(%.2f, %.2f, %.2f)", position.x, position.y, position.z) + ", map=" + (map == null ? "<NULL>" : map.getName()));
        if(getMap() == null) return;
        LoadedChunk oldChunk = getChunk();
        this.position = position;
        if(oldChunk != null) oldChunk.updateEntityReference(this);
        getChunk().updateEntityReference(this);
        positionChange = true;
    }

    @Override
    public void setMap(GameMap map) {
        if(this.map != null){
            this.map.removeEntity(this);
        }
        this.map = map;
        positionChange = true;
        rotationChange = true;
    }

    @Override
    public void setYaw(float yaw) {
        setRotation(yaw, getPitch());
    }

    @Override
    public void setPitch(float pitch) {
        setRotation(getYaw(), pitch);
    }

    public void setRotation(float yaw, float pitch){
        this.yaw = yaw;
        this.pitch = pitch;
        rotationChange = true;
    }

    public void setMeta(BinaryMetadata meta) {
        this.meta = meta;
        metaChange = true;
    }

    @Override
    public void tick() {
        if(positionChange || rotationChange) {
            org.dragonet.cloudland.net.protocol.Entity.ServerEntityUpdateMessage.Builder b = org.dragonet.cloudland.net.protocol.Entity.ServerEntityUpdateMessage.newBuilder();
            b.setEntityId(getEntityId());
            if(positionChange) {
                positionChange = false;
                b.setFlagPosition(true);
                b.setX(getPosition().x).setY(getPosition().y).setZ(getPosition().z);
            }
            if(rotationChange) {
                rotationChange = false;
                b.setFlagRotation(true);
                b.setYaw(yaw).setPitch(pitch);
            }
            if(metaChange) {
                metaChange = false;
                b.setFlagMeta(true);
                b.setMeta(meta != null ? meta.serialize() : Metadata.SerializedMetadata.getDefaultInstance());
            }
            org.dragonet.cloudland.net.protocol.Entity.ServerEntityUpdateMessage msg = b.build();
            LoadedChunk chunk = getChunk();
            int cx = chunk.getX();
            int cz = chunk.getZ();
            if(chunk != null) {
                getMap().forEachEntity((e) -> {
                    if (e != this && PlayerEntity.class.isAssignableFrom(e.getClass()) && ((PlayerEntity) e).isUsingChunk(cx, cz)) {
                        ((PlayerEntity) e).getSession().sendNetworkMessage(msg);
                    }
                });
            }
        }
    }

    public void markPositionChanged() {
        positionChange = true;
    }

    public void markRotationChanged(){
        rotationChange = true;
    }

    public void markMetaChanged(){
        metaChange = true;
    }

    @Override
    public abstract void spawnTo(PlayerEntity player);
}
