package org.dragonet.cloudland.server.item;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;
import lombok.Getter;

/**
 * Created on 2017/1/15.
 */
public enum Items {
    AIR(0), // can be merged, not requiring meta
    STONE(1),
    DIRT(2),
    GRASS(3),
    SAND(4),
    WATER(5),
    LOG(6),
    LEAVES(7)
    ;

    private static UnsignedLongKeyMap<Items> reverseMap = new UnsignedLongKeyMap<>(false);

    static {
        for(Items i : values()){
            long key = (i.id & 0xFFFFFFFFL) << 32 | (i.meta & 0xFFFFFFFFL);
            reverseMap.put(key, i);
        }
    }

    public static Items get(Item item){
        if(item == null) return null;
        return get(item.getId(), item.getMeta());
    }

    public static Items get(int id, int meta) {
        long key = (id & 0xFFFFFFFFL) << 32 | (meta & 0xFFFFFFFFL);
        return reverseMap.get(key);
    }

    @Getter
    private int id;

    @Getter
    private boolean needMeta;

    @Getter
    private int meta;

    @Getter
    private int maxStack;

    @Getter
    private boolean canBeMerged; // can it be merged to another item stack?

    /**
     * NOT requiring a meta
     * @param id
     */
    Items(int id) {
        this(id, 16); // we DO NOT requires meta by default
    }

    /**
     * Can be merged, not requiring meta
     * @param id
     */
    Items(int id, int maxStack) {
        this(id, 0, maxStack); // we DO NOT requires meta by default
        needMeta = false;
    }

    Items(int id, int meta, int maxStack){
        this(id, meta, maxStack, true); // CAN be merged by default
    }

    /**
     * Not requiring a meta
     * @param id
     * @param canBeMerged
     */
    Items(int id, int maxStack, boolean canBeMerged){
        this(id, 0, maxStack, canBeMerged);
        needMeta = false;
    }

    /**
     * REQUIRES meta
     * @param id
     * @param meta
     * @param canBeMerged
     */
    Items(int id, int meta, int maxStack, boolean canBeMerged) {
        this.id = id;
        this.meta = meta;
        needMeta = true;
        this.maxStack = maxStack;
        this.canBeMerged = canBeMerged;
    }

    public Item newItemInstance(int count) {
        return new Item(id, meta, count);
    }

    public BlockBehavior getBlockBehavior(){
        return BlockBehavior.get(id, meta);
    }
}
