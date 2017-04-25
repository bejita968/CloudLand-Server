package org.dragonet.cloudland.server.network;

import org.dragonet.cloudland.net.protocol.Metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/1/13.
 */
public class BinaryMetadata {

    public static class Keys {
        public final static int NAMETAG_STRING = 0;
        public final static int HOLDING_SLOT_INT = 1;

        public final static int ITEM_META = 0xBA;
    }

    private final Map<Integer, Metadata.SerializedMetadata.MetadataEntry> entries = new HashMap<>();

    public void putInt32(int key, int value) {
        if(entries.containsKey(key)) entries.remove(key);
        entries.put(key, Metadata.SerializedMetadata.MetadataEntry.newBuilder()
            .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT32)
            .setInt32Value(value).build());
    }

    public void putInt64(int key, long value) {
        if(entries.containsKey(key)) entries.remove(key);
        entries.put(key, Metadata.SerializedMetadata.MetadataEntry.newBuilder()
            .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.INT64)
            .build());
    }

    public void putString(int key, String value){
        if(entries.containsKey(key)) entries.remove(key);
        entries.put(key, Metadata.SerializedMetadata.MetadataEntry.newBuilder()
                .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.STRING)
                .setStringValue(value).build());
    }

    public void putBinaryMeta(int key, Metadata.SerializedMetadata meta) {
        if(entries.containsKey(key)) entries.remove(key);
        entries.put(key, Metadata.SerializedMetadata.MetadataEntry.newBuilder()
        .setType(Metadata.SerializedMetadata.MetadataEntry.DataType.META)
        .setMetaValue(meta)
        .build());
    }

    public boolean containsKey(int key) {
        return entries.containsKey(key);
    }

    public Metadata.SerializedMetadata.MetadataEntry.DataType getDataTyoe(int key){
        if(!entries.containsKey(key)) return null;
        return entries.get(key).getType();
    }

    public int getInt32(int key, int def){
        if(!containsKey(key)) return def;
        return entries.get(key).getInt32Value();
    }

    public long getInt64(int key, long def){
        if(!containsKey(key)) return def;
        return entries.get(key).getInt64Value();
    }

    public String getString(int key, String def) {
        if(!containsKey(key)) return def;
        return entries.get(key).getStringValue();
    }

    public Metadata.SerializedMetadata getMeta(int key, Metadata.SerializedMetadata def){
        if(!containsKey(key)) return def;
        return entries.get(key).getMetaValue();
    }

    public Metadata.SerializedMetadata serialize() {
        return Metadata.SerializedMetadata.newBuilder()
                .putAllEntries(entries)
                .build();
    }

}
