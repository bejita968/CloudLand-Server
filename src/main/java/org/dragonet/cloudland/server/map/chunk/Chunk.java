package org.dragonet.cloudland.server.map.chunk;

import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.math.Vector3;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2017/1/10.
 */
public class Chunk {
    public final static int SECTION_COUNT = 32;

    /**
     * this can be null
     */
    @Getter
    private final GameMap map;

    @Getter
    private final int x;

    @Getter
    private final int z;

    @Getter
    private boolean populated;

    @Getter
    private boolean generated;

    @Getter
    private ChunkSection[] sections;

    @Getter
    private Set<Vector3> dynamicBlocks;

    @Override
    public Chunk clone() {
        ChunkSection[] clonedSections = new ChunkSection[SECTION_COUNT];
        for(int y = 0; y < SECTION_COUNT; y++) {
            if(sections[y] == null) continue;
            clonedSections[y] = sections[y].clone();
        }
        return new Chunk(map, x, z, clonedSections);
    }

    public Chunk(GameMap map, int x, int z){
        this.map = map;
        this.x = x;
        this.z = z;
        this.sections = new ChunkSection[SECTION_COUNT];
        dynamicBlocks = new HashSet<>();
    }

    public Chunk(GameMap map, int x, int z, ChunkSection[] sections){
        this.map = map;
        this.x = x;
        this.z = z;
        this.sections = sections;
        dynamicBlocks = new HashSet<>();
    }

    public Chunk(GameMap map, int x, int z, ChunkSection[] sections, Set<Vector3> dynamicBlocks){
        this.map = map;
        this.x = x;
        this.z = z;
        this.sections = sections;
        this.dynamicBlocks = dynamicBlocks;
    }

    public ChunkSection getChunkSection(int y) {
        if(y >= SECTION_COUNT || y < 0) return null;
        return sections[y];
    }

    public int getBlock(int x, int y, int z){
        int cY = y >> 4;
        if(sections[cY] == null) return 0;
        return sections[cY].getBlock(x, y & 0xF, z);
    }

    public void setBlock(int x, int y, int z, int newId) {
        int cY = y >> 4;
        if(sections[cY] == null) sections[cY] = new ChunkSection(new byte[8192]);
        sections[cY].setBlock(x, y & 0xF, z, newId);
    }


    public void markPopulated() {
        populated = true;
    }

    public void markGenerated(){
        generated = true;
    }
}
