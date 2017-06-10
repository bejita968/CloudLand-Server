package org.dragonet.cloudland.server.entity;

import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.util.Vector3D;
import lombok.Getter;

/**
 * Created on 2017/1/19.
 */
public class ItemEntity extends StandaloneEntity implements Entity {

    @Getter
    private boolean alreadyPickedUp = false;

    @Getter
    public final Item item;

    public ItemEntity(Item item) {
        this.item = item;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void spawnTo(PlayerEntity player) {
        // Overridden base method
        if(item == null) {
            throw new IllegalArgumentException("dropped null item");
        }
        getMeta().putBinaryMeta(BinaryMetadata.Keys.ITEM_META, item.serializeToBinary());

        Vector3D pos = getPosition();
        player.getSession().sendNetworkMessage(org.dragonet.cloudland.net.protocol.Entity.ServerAddEntityMessage.newBuilder()
                .setEntityId(getEntityId())
                .setEntityType(org.dragonet.cloudland.net.protocol.Entity.EntityType.ITEM)
                .setX(pos.x)
                .setY(pos.y)
                .setZ(pos.z)
                .setYaw(getYaw())
                .setPitch(getPitch())
                .setMeta(getMeta().serialize())
                .build());
    }

    public void pickUpBy(PlayerEntity e){

    }
}
