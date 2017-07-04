package org.dragonet.cloudland.server.entity;

import com.google.protobuf.Message;
import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.Vector3D;
import org.dragonet.cloudland.server.util.math.Vector3f;
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

    void broadcastToViewers(Message message);

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
     * MUST be parented first
     * @param slot
     * @return success or not
     */
    boolean takeSlot(Entity parent, int slot);

    /**
     * quit that slot, eg. stand up from a driver seat.
     * @return where should this entity go? (described more in the ProtoBuf sources)
     */
    void quitSlot();

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
