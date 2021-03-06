package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.map.GameMap;

import java.util.Random;

public class OreVein {

    private final int type;
    private final int amount;
    private final int targetType;

    public OreVein(OreType oreType) {
        type = oreType.getType();
        amount = oreType.getAmount();
        targetType = oreType.getTargetType();
    }

    public void generate(GameMap world, Random random, int sourceX, int sourceY, int sourceZ) {
        float angle = random.nextFloat() * (float) Math.PI;
        double dx1 = sourceX + Math.sin(angle) * amount / 8.0F;
        double dx2 = sourceX - Math.sin(angle) * amount / 8.0F;
        double dz1 = sourceZ + Math.cos(angle) * amount / 8.0F;
        double dz2 = sourceZ - Math.cos(angle) * amount / 8.0F;
        double dy1 = sourceY + random.nextInt(3) - 2;
        double dy2 = sourceY + random.nextInt(3) - 2;

        for (int i = 0; i < amount; i++) {
            double originX = dx1 + (dx2 - dx1) * i / amount;
            double originY = dy1 + (dy2 - dy1) * i / amount;
            double originZ = dz1 + (dz2 - dz1) * i / amount;
            double q = random.nextDouble() * amount / 16.0D;
            double hRadius = (Math.sin(i * (float) Math.PI / amount) + 1 * q + 1) / 2.0D;
            double vRadius = (Math.sin(i * (float) Math.PI / amount) + 1 * q + 1) / 2.0D;

            int startY = (int) (originY - vRadius);
            if(startY < 0) startY = 0; // idk why but we should add this

            for (int x = (int) (originX - hRadius); x <= (int) (originX - hRadius); x++) {
                double pX = (x + 0.5D - originX) / hRadius;
                pX *= pX;
                if (pX < 1) {
                    for (int y = startY; y <= (int) (originY + vRadius); y++) {
                        double pY = (y + 0.5D - originY) / vRadius;
                        pY *= pY;
                        if (pX + pY < 1) {
                            for (int z = (int) (originZ - hRadius); z <= (int) (originZ + hRadius); z++) {
                                double pZ = (z + 0.5D - originZ) / hRadius;
                                pZ *= pZ;
                                if (pX + pY + pZ < 1 && world.getBlockAt(x, y, z) == targetType) {
                                    /*
                                    BlockState state = world.getBlockAt(x, y, z).getState();
                                    state.setType(type);
                                    state.setData(data);
                                    state.update(true);
                                    */
                                    world.setBlockAt(x, y, z, type);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
