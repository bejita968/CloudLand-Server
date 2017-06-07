package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.Vector3D;
import org.dragonet.cloudland.server.util.math.Vector3f;

import org.dragonet.cloudland.net.protocol.Entity.ServerEntityHierarchicalControlMessage.HierarchicalAction;

/**
 * Created on 2017/1/10.
 */
public interface Entity {

    long getEntityId();

    void setEntityId(long entityId);

    GameMap getMap();

    void setMap(GameMap map);

    LoadedChunk getChunk();

    Vector3D getPosition();

    void setPosition(Vector3D position);

    float getYaw();

    float getPitch();

    void setYaw(float yaw);

    void setPitch(float pitch);

    void spawnTo(PlayerEntity player);

    /**
     * entities meta, shared with client
     * @return
     */
    BinaryMetadata getMeta();

    /**
     * called per tick
     */
    void tick();

    /**
     * tick NOT as a child
     */
    void tickNormal();

    /**
     * tick as a child in another entity
     */
    void tickChild();

    /**
     * as a passenger as a slot?
     * @return
     */
    boolean hasParent();

    /**
     * this entity may just being a passenger or in a slot.
     * @return
     */
    Entity getParent();

    /**
     * check whether it has a child
     * @param entityId
     * @return
     */
    boolean hasChild(Entity entity);

    /**
     * add a child
     * @param entity
     */
    void addChild(Entity entity);

    /**
     * removes a child
     * @param entity
     */
    void removeChild(Entity entity);

    /**
     * let this entity enter the parent entity,
     * will not let this entity take slot, just enter.
     * @param parent
     */
    void setParent(Entity parent);

    /**
     * have slots?
     * @return
     */
    boolean hasEntitySlots();

    /**
     * how many slots?
     * @return
     */
    int getEntitySlots();

    /**
     * where is that slot?
     * @param index
     * @return
     */
    Vector3f getEntitySlotRelativePosition(int index);

    /**
     * take a slot, eg. sitting on a driver seat.
     * @param superEntity
     * @param slot
     */
    void takeSlot(Entity superEntity, int slot);

    /**
     * quit that slot, eg. stand up from a driver seat.
     * @return where should this entity go? (described more in the ProtoBuf sources)
     */
    Vector3f quitSlot(HierarchicalAction action);

    /**
     * are we in another entity and taking a slot?
     * @return
     */
    boolean isInSlot();

    /**
     * where are we?
     * @return
     */
    int getSlotIndexTaken();
}
