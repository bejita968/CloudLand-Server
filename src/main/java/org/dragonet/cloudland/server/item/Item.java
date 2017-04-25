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
    public final static Item AIR = new Item(0, 0, 0);
    public final static Metadata.SerializedMetadata AIR_SERIALIZED = AIR.serializeToBinary();

    @Getter
    private int id;

    @Getter
    private int meta;

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
     * MUST have this constructor since Items uses it!
     * @param id
     * @param meta
     * @param count
     */
    public Item(int id, int meta, int count) {
        this.id = id;
        this.meta = meta;
        this.count = count;
    }

    public Inventory.SerializedItem serialize() {
        return Inventory.SerializedItem.newBuilder()
                .setId(id)
                .setMeta(meta)
                .setCount(count)
                .setBinaryMeta(binaryMeta != null ? binaryMeta.serialize() : Metadata.SerializedMetadata.getDefaultInstance())
                .build();
    }

    public Metadata.SerializedMetadata serializeToBinary(){
        Metadata.SerializedMetadata.Builder b = Metadata.SerializedMetadata.newBuilder();
        Metadata.SerializedMetadata.MetadataEntry serializedId = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT32)
                .setInt32Value(id).build();
        Metadata.SerializedMetadata.MetadataEntry serializedMeta = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT32)
                .setInt32Value(meta).build();
        Metadata.SerializedMetadata.MetadataEntry serializedCount = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT32)
                .setInt32Value(count).build();
        Metadata.SerializedMetadata.MetadataEntry serializedBinaryMeta = Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.META)
                .setMetaValue(binaryMeta == null ? Metadata.SerializedMetadata.getDefaultInstance() : binaryMeta.serialize()).build();
        b.putEntries(0, serializedId);
        b.putEntries(1, serializedMeta);
        b.putEntries(2, serializedCount);
        b.putEntries(3, serializedBinaryMeta);
        return b.build();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !Item.class.isAssignableFrom(obj.getClass())) return false;
        Item target = (Item)obj;
        if(!Items.get(id, meta).isCanBeMerged()) return false;
        if(!Items.get(target.id, target.meta).isCanBeMerged()) return false;
        return this.id == target.id && (!Items.get(id, meta).isNeedMeta() || this.meta == target.meta);
    }

    /**
     * Will NOT copy binary meta
     * @return
     */
    @Override
    public Item clone() {
        return new Item(id, meta, count);
    }

    public boolean isMergeableWith(Item item) {
        // we should write behaviors in this method
        if(item == null) return false;

        // add exceptions here
        // ...

        // or just check ID and Meta by default
        if(id == item.id && meta == item.meta) {
            return true;
        } else {
            return false;
        }
    }
}
