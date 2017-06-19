package org.dragonet.cloudland.server.entity;

import com.google.protobuf.Message;
import org.dragonet.cloudland.net.protocol.DataTypes;
import org.dragonet.cloudland.net.protocol.Metadata;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;
import org.dragonet.cloudland.server.util.Vector3D;
import lombok.Getter;
import org.dragonet.cloudland.net.protocol.Entity.ServerEntityHierarchicalControlMessage.HierarchicalAction;
import org.dragonet.cloudland.server.util.math.Vector3f;

import java.util.HashSet;
import java.util.Set;

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

    protected UnsignedLongKeyMap<PlayerEntity> entityHolders = new UnsignedLongKeyMap<>(false);

    @Getter
    private float yaw;

    @Getter
    private float pitch;

    private boolean positionChange = false;
    private boolean rotationChange = false;
    private boolean metaChange = false;

    private int slotTaken = -1;
    private Entity parent;
    private Set<Long> children = new HashSet<>();

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

    @Override
    public void broadcastToViewers(Message message) {
        entityHolders.forEachValue((e) -> e.getSession().sendNetworkMessage(message));
    }

    /* ====== Hierarchical Management (ye, let's make it fancy) ====== */

    @Override
    public boolean isInSlot() {
        return slotTaken != -1;
    }

    @Override
    public int getSlotIndexTaken() {
        return slotTaken;
    }

    @Override
    public Entity getParent() {
        return parent;
    }

    @Override
    public boolean hasParent() {
        return parent != null;
    }

    @Override
    public boolean hasChild(Entity entity) {
        return children.contains(entity.getEntityId());
    }

    @Override
    public void addChild(Entity entity, int gateIndex) {
        if(!this.enterable()) return;
        if(entity.hasParent() && entity.getParent() != null) {
            entity.setParent(this, gateIndex);
            return;
        }

        this.children.add(entity.getEntityId());

        if(entity.getParent() != this) {
            entity.setParent(this, gateIndex);
        }

        Vector3f gateInPos = getGatePosition(gateIndex, true);

        broadcastToViewers(org.dragonet.cloudland.net.protocol.Entity.ServerEntityHierarchicalControlMessage.newBuilder()
                .setEntityId(entity.getEntityId())
                .setTargetEntityId(entityId)
                .setAction(HierarchicalAction.ENTER)
                .setPosition(gateInPos.encodeToNetwork()).build());
    }

    @Override
    public void removeChild(Entity entity, int gateIndex) {
        if(!this.enterable()) return;
        if(entity.hasParent() && entity.getParent() != null) {
            return;
        }
        this.children.remove(entity.getEntityId());

        if(entity.getParent() != null) {
            entity.setParent(null, gateIndex);
        }

        Vector3f gateOutPos = getGatePosition(gateIndex, false);

        broadcastToViewers(org.dragonet.cloudland.net.protocol.Entity.ServerEntityHierarchicalControlMessage.newBuilder()
                .setEntityId(entity.getEntityId())
                .setTargetEntityId(entityId)
                .setAction(HierarchicalAction.LEAVING_TO_OUTSIDE)
                .setPosition(gateOutPos.encodeToNetwork()).build());
    }

    @Override
    public void setParent(Entity parent, int gateIndex) {
        if(this.parent != null) {
            Entity ref = this.parent;
            Vector3D refPos = ref.getPosition();
            Vector3f gateOutPos = parent.getGatePosition(gateIndex, false);
            this.parent = null;
            // first, we disable child, set it to a normal entity
            if(ref.hasChild(this)) {
                ref.removeChild(this, gateIndex);

                broadcastToViewers(org.dragonet.cloudland.net.protocol.Entity.ServerEntityHierarchicalControlMessage.newBuilder()
                        .setEntityId(entityId)
                        .setTargetEntityId(0L)
                        .setAction(HierarchicalAction.LEAVING_TO_OUTSIDE)
                        .setPosition(gateOutPos.encodeToNetwork()).build());
            }

            if(parent == null || !parent.enterable()) {
                return;
            }
        }

        // where to go when enter from a gate
        Vector3f gateInPos = parent.getGatePosition(gateIndex, true);

        this.parent = parent;
        if(!parent.hasChild(this)) {
            parent.addChild(this, gateIndex);

            broadcastToViewers(org.dragonet.cloudland.net.protocol.Entity.ServerEntityHierarchicalControlMessage.newBuilder()
                    .setEntityId(entityId)
                    .setTargetEntityId(0L)
                    .setAction(HierarchicalAction.ENTER)
                    .setPosition(gateInPos.encodeToNetwork())
                    .build());
        }
    }

    @Override
    public boolean takeSlot(int slot) {
        if(parent == null) return false;
        if(slot >= parent.getEntitySlots()) return false;
        // don't need to acquire this since client will use its own position
        // Vector3f pos = parent.getEntitySlotRelativePosition(slot);

        broadcastToViewers(org.dragonet.cloudland.net.protocol.Entity.ServerEntityBindingControlMessage.newBuilder()
        .setEntityId(entityId)
        .setSlotId(slot)
        .setTargetEntityId(parent.getEntityId()).build());
        return true;
    }

    @Override
    public void tickNormal() {
        // default nothing
    }

    @Override
    public void tickChild() {
        // default nothing
    }
}
