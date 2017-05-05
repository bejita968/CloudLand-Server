package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.item.ItemPrototype;

/**
 * Created on 2017/2/26.
 */
public class OreType {
    public final ItemPrototype material;
    public final int clusterCount;
    public final int clusterSize;
    public final int maxHeight;
    public final int minHeight;

    public OreType(ItemPrototype material, int clusterCount, int clusterSize, int minHeight, int maxHeight) {
        this.material = material;
        this.clusterCount = clusterCount;
        this.clusterSize = clusterSize;
        this.maxHeight = maxHeight;
        this.minHeight = minHeight;
    }
}