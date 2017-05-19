package org.dragonet.cloudland.server.behavior;

import org.dragonet.cloudland.server.behavior.block.*;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.util.Direction;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/1/17.
 */
public abstract class BlockBehavior {


    private static final Map<Integer, BlockBehavior> register = new HashMap<>();

    private static boolean initiated = false;
    public static void init() {
        if(initiated) return;

        register(ItemPrototype.toId("cloudland:stone"), new StoneBehavior());
        register(ItemPrototype.toId("cloudland:dirt"), new DirtBehavior());
        register(ItemPrototype.toId("cloudland:grass"), new DirtBehavior());
        register(ItemPrototype.toId("cloudland:sand"), new SandBehavior());
        register(ItemPrototype.toId("cloudland:water"), new WaterBehavior());
        register(ItemPrototype.toId("cloudland:log"), new LogBehavior());
        register(ItemPrototype.toId("cloudland:leaves"), new LeavesBehavior());
        register(ItemPrototype.toId("cloudland:plank"), new PlankBehavior());
        register(ItemPrototype.toId("cloudland:torch"), new TorchBehavior());

        initiated = true;
    }

    private static void register(ItemPrototype item, BlockBehavior behavior) {
        register(item.getId(), behavior);
    }

    private static void register(int block, BlockBehavior behavior){
        register.put(block, behavior);
    }

    public static BlockBehavior get(ItemPrototype item) {
        return get(item.getId());
    }

    public static BlockBehavior get(int block) {
        return register.get(block);
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
