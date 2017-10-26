package org.dragonet.cloudland.server.item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dragonet.cloudland.server.behavior.BlockBehavior;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/1/15.
 */
@AllArgsConstructor
public final class ItemPrototype {

    private final static Map<String, ItemPrototype> nameMap = new HashMap<>();
    private final static Map<Integer, ItemPrototype> idMap = new HashMap<>();
    private final static Map<Integer, String> idToName = new HashMap<>();
    private final static Map<String, Integer> nameToId = new HashMap<>();

    private static boolean initiated = false;
    public static void init() {
        if(initiated) return;

        // Register internal definitions
        // blocks
        register(0, "cloudland:air", createSimpleItem(0, 0));
        register(1, "cloudland:stone", createSimpleBlock(1, 16));
        register(2, "cloudland:dirt", createSimpleBlock(2, 16));
        register(3, "cloudland:grass", createSimpleBlock(3, 16));
        register(4, "cloudland:sand", createSimpleBlock(4, 16));
        register(5, "cloudland:water", createSimpleBlock(5, 16));
        register(6, "cloudland:log", createSimpleBlock(6, 16));
        register(7, "cloudland:leaves", createSimpleBlock(7, 16));
        register(8, "cloudland:plank", createSimpleBlock(8, 32));
        register(9, "cloudland:torch", createSimpleBlock(9, 64));
        register(10, "cloudland:crafting_table", createSimpleBlock(10, 64));

        // items
        register(32768, "cloudland:wood_stick", createSimpleItem(32768, 64));
        // tools
        register(32769, "cloudland:wood_axe", createSimpleItem(32769, 1));

        initiated = true;
    }

    @Getter
    private static boolean locked = false;  // after server started, all modifications will be denied
    public static void lock(){
        locked = true;
    }
    public static void register(int id, String name, ItemPrototype prototype) {
        if(locked) throw new IllegalStateException("unable to register item definitions after server started, consider using a mod. ");
        if(idMap.containsKey(id)) throw new IllegalStateException("item id already taken! ");
        if(nameMap.containsKey(name)) throw new IllegalStateException("item name already taken! ");
        idMap.put(id, prototype);
        nameMap.put(name, prototype);
        idToName.put(id, name);
        nameToId.put(name, id);
    }

    // ========================== HELPER STATIC METHODS ==========================

    /**
     * Get a item prototype by its ID.
     * @param id
     * @return
     */
    public static ItemPrototype get(int id) {
        return idMap.get(id);
    }

    /**
     * Get a item prototype by its name.
     * @param name
     * @return
     */
    public static ItemPrototype get(String name) {
        return nameMap.get(name);
    }

    /**
     * Get a item prototype by an item instance.
     * @param item
     * @return
     */
    public static ItemPrototype get(Item item) {
        return get(item.getId());
    }

    /**
     * Convert a item name to its id.
     * @param name
     * @return
     */
    public static int toId(String name) {
        if(!nameToId.containsKey(name)) return 0;
        return nameToId.get(name);
    }

    /**
     * Converts a item name to its id.
     * @param id
     * @return
     */
    public static String toName(int id) {
        if(!idToName.containsKey(id)) return "cloudland:air";
        return idToName.get(id);
    }

    /**
     * Create a item prototype that does NOT requires binary meta data.
     * @param id
     * @param maxStack
     * @return
     */
    public static ItemPrototype createSimpleItem(int id, int maxStack) {
        if(locked) throw new IllegalStateException("can not create item prototypes after server started, consider using a mod. ");
        return createItem(id, maxStack, false);
    }

    /**
     * Create a item prototype.
     * @param id
     * @param maxStack
     * @param binaryMetaRequired
     * @return
     */
    public static ItemPrototype createItem(int id, int maxStack, boolean binaryMetaRequired) {
        if(locked) throw new IllegalStateException("can not create item prototypes after server started, consider using a mod. ");
        return new ItemPrototype(id, false, maxStack, binaryMetaRequired);
    }

    /**
     * Create a block prototype that does NOT requires binary meta data.
     * @param id
     * @param maxStack
     * @return
     */
    public static ItemPrototype createSimpleBlock(int id, int maxStack) {
        if(locked) throw new IllegalStateException("can not create item prototypes after server started, consider using a mod. ");
        return createBlock(id, maxStack, false);
    }

    /**
     * Create a block prototype.
     * @param id
     * @param maxStack
     * @param binaryMetaRequired
     * @return
     */
    public static ItemPrototype createBlock(int id, int maxStack, boolean binaryMetaRequired) {
        if(locked) throw new IllegalStateException("can not create item prototypes after server started, consider using a mod. ");
        return new ItemPrototype(id, true, maxStack, binaryMetaRequired);
    }

    // ========================== INSTANCE VARIABLES AND METHODS ==========================

    @Getter
    private int id;

    @Getter
    private boolean block;

    @Getter
    private int maxStack = 16;

    @Getter
    private boolean binaryMetaRequired;

    public boolean canBeMergedTo(ItemPrototype target) {
        if(target == null) return false;
        if(target.id != id) return false;
        if(binaryMetaRequired) return false;
        return true;
    }

    public BlockBehavior getBlockBehavior(){
        if(!block) return null;
        return BlockBehavior.get(id);
    }

    public Item newItemInstance(int count ){
        return new Item(id, count);
    }
}
