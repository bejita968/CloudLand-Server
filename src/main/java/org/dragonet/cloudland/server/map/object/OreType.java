package org.dragonet.cloudland.server.map.object;

/**
 * Created on 2017/2/26.
 */
public class OreType {
    private final int type;
    private final int minY;
    private final int maxY;
    private final int amount;
    private final int targetType;

    public OreType(int type, int minY, int maxY, int amount, int targetType) {
        this.type = type;
        this.minY = minY;
        this.maxY = maxY;
        this.amount = ++amount;
        this.targetType = targetType;
    }

    public int getType() {
        return type;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getAmount() {
        return amount;
    }

    public int getTargetType() {
        return targetType;
    }
}