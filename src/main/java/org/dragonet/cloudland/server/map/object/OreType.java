package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.item.Items;

/**
 * Created on 2017/2/26.
 */
public class OreType {
    public final Items material;
    public final int clusterCount;
    public final int clusterSize;
    public final int maxHeight;
    public final int minHeight;

    public OreType(Items material, int clusterCount, int clusterSize, int minHeight, int maxHeight) {
        this.material = material;
        this.clusterCount = clusterCount;
        this.clusterSize = clusterSize;
        this.maxHeight = maxHeight;
        this.minHeight = minHeight;
    }
}