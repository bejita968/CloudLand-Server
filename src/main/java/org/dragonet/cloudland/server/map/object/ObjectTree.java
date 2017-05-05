package org.dragonet.cloudland.server.map.object;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.ChunkManager;
import org.dragonet.cloudland.server.util.NukkitRandom;

import java.util.HashMap;
import java.util.Map;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class ObjectTree {

    private final static ItemPrototype LOG = ItemPrototype.get("cloudland:log");
    private final static ItemPrototype LEAVES = ItemPrototype.get("cloudland:leaves");

    private final static int DIRT_ID = ItemPrototype.toId("cloudland:dirt");
    private final static int LOG_ID = ItemPrototype.toId("cloudland:log");
    private final static int LEAVES_ID = ItemPrototype.toId("cloudland:leaves");

    public final Map<Integer, Boolean> overridable = new HashMap<Integer, Boolean>() {
        {
            put(0, true);
            // put(Block.SAPLING, true);
            put(LOG_ID, true);
            put(LEAVES_ID, true);
            // put(Block.SNOW_LAYER, true);
            // put(Block.LOG2, true);
            // put(Block.LEAVES2, true);
        }
    };

    public int getType() {
        return 0;
    }

    public ItemPrototype getTrunkBlock() {
        return LOG;
    }

    public ItemPrototype getLeafBlock() {
        return LEAVES;
    }

    public int getTreeHeight() {
        return 7;
    }

    public static void growTree(ChunkManager map, int x, int y, int z, NukkitRandom random) {
        growTree(map, x, y, z, random, 0);
    }

    public static void growTree(ChunkManager map, int x, int y, int z, NukkitRandom random, int type) {
        ObjectTree tree;
        switch (type) {
            /*
            case BlockSapling.SPRUCE:
                if (random.nextBoundedInt(39) == 0) {
                    tree = new ObjectSpruceTree();
                } else {
                    tree = new ObjectSpruceTree();
                }
                break;
            case BlockSapling.BIRCH:
                if (random.nextBoundedInt(39) == 0) {
                    tree = new ObjectTallBirchTree();
                } else {
                    tree = new ObjectBirchTree();
                }
                break;
            case BlockSapling.JUNGLE:
                tree = new ObjectJungleTree();
                break;
            case BlockSapling.OAK:*/
            default:
                tree = new ObjectOakTree();
                //todo: more complex treeeeeeeeeeeeeeeee
                break;
        }

        if (tree.canPlaceObject(map, x, y, z, random)) {
            tree.placeObject(map, x, y, z, random);
        }
    }


    public boolean canPlaceObject(ChunkManager map, int x, int y, int z, NukkitRandom random) {
        int radiusToCheck = 0;
        for (int yy = 0; yy < this.getTreeHeight() + 3; ++yy) {
            if (yy == 1 || yy == this.getTreeHeight()) {
                ++radiusToCheck;
            }
            for (int xx = -radiusToCheck; xx < (radiusToCheck + 1); ++xx) {
                for (int zz = -radiusToCheck; zz < (radiusToCheck + 1); ++zz) {
                    if (!this.overridable.containsKey(map.getBlockAt(x + xx, y + yy, z + zz))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void placeObject(ChunkManager map, int x, int y, int z, NukkitRandom random) {

        this.placeTrunk(map, x, y, z, random, this.getTreeHeight() - 1);

        for (int yy = y - 3 + this.getTreeHeight(); yy <= y + this.getTreeHeight(); ++yy) {
            double yOff = yy - (y + this.getTreeHeight());
            int mid = (int) (1 - yOff / 2);
            for (int xx = x - mid; xx <= x + mid; ++xx) {
                int xOff = Math.abs(xx - x);
                for (int zz = z - mid; zz <= z + mid; ++zz) {
                    int zOff = Math.abs(zz - z);
                    if (xOff == mid && zOff == mid && (yOff == 0 || random.nextBoundedInt(2) == 0)) {
                        continue;
                    }
                    int id = map.getBlockAt(xx, yy, zz);
                    if (id == 0 || !BlockBehavior.get(id).isSolid()) {
                        map.setBlockAt(xx, yy, zz, getLeafBlock().getId());
                    }
                }
            }
        }
    }

    protected void placeTrunk(ChunkManager map, int x, int y, int z, NukkitRandom random, int trunkHeight) {
        // The base dirt block
        map.setBlockAt(x, y - 1, z, DIRT_ID);

        for (int yy = 0; yy < trunkHeight; ++yy) {
            int blockId = map.getBlockAt(x, y + yy, z);
            if (this.overridable.containsKey(blockId)) {
                map.setBlockAt(x, y + yy, z, getTrunkBlock().getId());
            }
        }
    }
}
