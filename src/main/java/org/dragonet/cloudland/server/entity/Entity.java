package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.Vector3D;

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

    BinaryMetadata getMeta();

    void tick();
}
