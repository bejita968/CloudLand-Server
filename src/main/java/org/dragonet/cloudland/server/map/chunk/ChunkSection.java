package org.dragonet.cloudland.server.map.chunk;

import lombok.Getter;

import java.util.Arrays;

/**
 * Created on 2017/1/10.
 */
public class ChunkSection {

    public final static byte[] EMPTY_ARRAY = new byte[8192];

    public static int pos2index(int x, int y, int z) {
        return ((z & 0xF) << 8 | (x & 0xF) << 4 | (y & 0xF)) * 2;
    }

    @Getter
    private byte[] ids;

    @Getter
    private byte[] meta;

    public ChunkSection(byte[] ids, byte[] meta) {
        this.ids = ids;
        this.meta = meta;
    }

    public int getBlockId(int x, int y, int z){
        int idx = pos2index(x, y, z);
        return (ids[idx] & 0xFF) << 8 | (ids[idx+1] & 0xFF);
    }

    public int getBlockMeta(int x, int y, int z){
        int idx = pos2index(x, y, z);
        return (meta[idx] & 0xFF) << 8 | (meta[idx+1] & 0xFF);
    }

    public void setBlock(int x, int y, int z, int newId, int newMeta) {
        int idx = pos2index(x, y, z);
        ids[idx] = (byte)((newId >> 8) & 0xFF);
        ids[idx+1] = (byte)(newId & 0xFF);
        meta[idx] = (byte)((newMeta >> 8) & 0xFF);
        meta[idx+1] = (byte)(newMeta & 0xFF);
    }

    public void setBlockId(int x, int y, int z, int newId){
        int idx = pos2index(x, y, z);
        ids[idx] = (byte)((newId >> 8) & 0xFF);
        ids[idx+1] = (byte)(newId & 0xFF);
    }

    public void setBlockMeta(int x, int y, int z, int newMeta) {
        int idx = pos2index(x, y, z);
        meta[idx] = (byte)((newMeta >> 8) & 0xFF);
        meta[idx+1] = (byte)(newMeta & 0xFF);
    }

    @Override
    protected ChunkSection clone() {
        byte[] c_ids = Arrays.copyOf(ids, ids.length);
        byte[] c_meta = Arrays.copyOf(meta, meta.length);
        return new ChunkSection(c_ids, c_meta);
    }
}
