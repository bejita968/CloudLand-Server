package org.dragonet.cloudland.server.behavior;

import org.dragonet.cloudland.server.behavior.block.*;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.Items;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.Direction;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;

/**
 * Created on 2017/1/17.
 */
public abstract class BlockBehavior {


    private static final UnsignedLongKeyMap<BlockBehavior> register = new UnsignedLongKeyMap<>(false);

    static {
        register(Items.STONE, new StoneBehavior());
        register(Items.DIRT, new DirtBehavior());
        register(Items.GRASS, new DirtBehavior());
        register(Items.SAND, new SandBehavior());
        register(Items.WATER, new WaterBehavior());
        register(Items.LOG, new LogBehavior());
        register(Items.LEAVES, new LeavesBehavior());
    }

    private static void register(Items item, BlockBehavior behavior) {
        register(item.getId(), item.getMeta(), behavior);
    }

    private static void register(int block, int meta, BlockBehavior behavior){
        long key = (block & 0xFFFFFFFFL) << 32 | (meta & 0xFFFFFFFFL);
        register.put(key, behavior);
    }

    public static BlockBehavior get(Items item) {
        return get(item.getId(), item.getMeta());
    }

    public static BlockBehavior get(int block, int meta) {
        long key = (block & 0xFFFFFFFFL) << 32 | (meta & 0xFFFFFFFFL);
        if(!register.containsKey(key)) return null;
        return register.get(key);
    }

    public boolean onTouch(PlayerEntity player, GameMap map, int x, int y, int z, Direction direction, Item tool) {
        return true;
    }

    public Item onPlace(PlayerEntity player, GameMap map, int x, int y, int z, Item tool){
        return null;
    }

    public boolean onStartBreak(PlayerEntity player, GameMap map, int x, int y, int z, Item tool){
        return true;
    }

    public boolean onEndBreak(PlayerEntity player, GameMap map, int x, int y, int z, Item tool, long breakTime){
        if(breakTime >= getBreakTime(tool)) return true;
        return false;
    }

    public Item[] getDrops(Item tool){
        return null;
    }

    public long getBreakTime(Item tool) {
        return 500;
    }

    public boolean isTransparent(){
        return false;
    }

    public boolean isSolid(){
        return true;
    }
}
