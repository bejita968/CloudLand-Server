package org.dragonet.cloudland.server.item;

import org.dragonet.cloudland.net.protocol.Inventory;
import org.dragonet.cloudland.net.protocol.Metadata;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2017/1/15.
 */
public class Item {

    /**
     * used to replace NULLs in networking
     */
    public final static Item AIR = new Item(0, 0);
    public final static Metadata.SerializedMetadata AIR_SERIALIZED = AIR.serializeToBinary();

    @Getter
    private int id;

    @Getter
    @Setter
    private int count;

    /**
     * Could be NULL!!
     */
    @Getter
    @Setter
    private BinaryMetadata binaryMeta;

    /**
     * MUST have this constructor since ItemPrototype uses it!
     * @param id
     * @param count
     */
    public Item(int id, int count) {
        this.id = id;
        this.count = count;
    }

    public Inventory.SerializedItem serialize() {
        return Inventory.SerializedItem.newBuilder()
                .setId(id)
                .setCount(count)
                .setBinaryMeta(binaryMeta != null ? binaryMeta.serialize() : Metadata.SerializedMetadata.getDefaultInstance())
                .build();
    }

    public Metadata.SerializedMetadata serializeToBinary(){
        Metadata.SerializedMetadata.Builder b = Metadata.SerializedMetadata.newBuilder();
        Metadata.SerializedMetadata.MetadataEntry serializedId = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT32)
                .setInt32Value(id).build();
        Metadata.SerializedMetadata.MetadataEntry serializedCount = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT32)
                .setInt32Value(count).build();
        Metadata.SerializedMetadata.MetadataEntry serializedBinaryMeta = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.META)
                .setMetaValue(binaryMeta == null ? Metadata.SerializedMetadata.getDefaultInstance() : binaryMeta.serialize()).build();
        b.putEntries(0, serializedId);
        b.putEntries(1, serializedCount);
        b.putEntries(2, serializedBinaryMeta);
        return b.build();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !Item.class.isAssignableFrom(obj.getClass())) return false;
        Item target = (Item)obj;
        ItemPrototype targetPrototype = ItemPrototype.get(target.id);
        if(!ItemPrototype.get(target.id).canBeMergedTo(targetPrototype)) return false;
        return this.id == target.id;
    }

    /**
     * Will NOT copy binary meta
     * @return
     */
    @Override
    public Item clone() {
        return new Item(id, count);
    }

    public boolean isMergeableWith(Item item) {
        // we should write behaviors in this method
        if(item == null) return false;

        // add exceptions here
        // ...

        // or just check ID and Meta by default
        if(id == item.id) {
            return true;
        } else {
            return false;
        }
    }
}
