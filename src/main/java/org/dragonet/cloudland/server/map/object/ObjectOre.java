package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.NukkitRandom;
import org.dragonet.cloudland.server.util.math.Vector2;
import org.dragonet.cloudland.server.util.math.VectorMath;

/**
 * Created on 2017/2/26.
 */
public class ObjectOre {

    private final static int STONE_ID = ItemPrototype.toId("cloudland:stone");

    private final NukkitRandom random;
    public final OreType type;
    private int replaceId;

    public ObjectOre(NukkitRandom random, OreType type) {
        this(random, type, STONE_ID);
    }

    public ObjectOre(NukkitRandom random, OreType type, int replaceId) {
        this.type = type;
        this.random = random;
        this.replaceId = replaceId;
    }

    public OreType getType() {
        return type;
    }

    public boolean canPlaceObject(GameMap level, int x, int y, int z) {
        return (level.getBlockAt(x, y, z) == replaceId);
    }

    public void placeObject(GameMap level, int x, int y, int z) {
        int clusterSize = this.type.clusterSize;
        double angle = this.random.nextFloat() * Math.PI;
        Vector2 offset = VectorMath.getDirection2D(angle).multiply(clusterSize).divide(8);
        double x1 = x + 8 + offset.x;
        double x2 = x + 8 - offset.x;
        double z1 = z + 8 + offset.y;
        double z2 = z + 8 - offset.y;
        double y1 = y + this.random.nextBoundedInt(3) + 2;
        double y2 = y + this.random.nextBoundedInt(3) + 2;
        for (int count = 0; count <= clusterSize; ++count) {
            double seedX = x1 + (x2 - x1) * count / clusterSize;
            double seedY = y1 + (y2 - y1) * count / clusterSize;
            double seedZ = z1 + (z2 - z1) * count / clusterSize;
            double size = ((Math.sin(count * (Math.PI / clusterSize)) + 1) * this.random.nextFloat() * clusterSize / 16 + 1) / 2;

            int startX = (int) (seedX - size);
            int startY = (int) (seedY - size);
            int startZ = (int) (seedZ - size);
            int endX = (int) (seedX + size);
            int endY = (int) (seedY + size);
            int endZ = (int) (seedZ + size);

            for (x = startX; x <= endX; ++x) {
                double sizeX = (x + 0.5 - seedX) / size;
                sizeX *= sizeX;

                if (sizeX < 1) {
                    for (y = startY; y <= endY; ++y) {
                        double sizeY = (y + 0.5 - seedY) / size;
                        sizeY *= sizeY;

                        if (y > 0 && (sizeX + sizeY) < 1) {
                            for (z = startZ; z <= endZ; ++z) {
                                double sizeZ = (z + 0.5 - seedZ) / size;
                                sizeZ *= sizeZ;

                                if ((sizeX + sizeY + sizeZ) < 1 && level.getBlockAt(x, y, z) == replaceId) {
                                    level.setBlockAt(x, y, z, this.type.material.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
