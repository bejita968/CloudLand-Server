package org.dragonet.cloudland.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/6/25.
 */
public class EntityType {

    private final static Map<Integer, EntityType> idMap = new HashMap<>();
    private final static Map<String, EntityType> nameMap = new HashMap<>();

    private static boolean initialized = false;
    private static boolean locked = false;

    public static void init(){
        if(initialized) return;
        register(new EntityType(1, "item"));
        register(new EntityType(2, "item"));

        register(new EntityType(100, "item"));
    }

    public static void register(EntityType type){
        if(locked) throw new IllegalStateException("already locked, please register using mods before server loaded! ");
        idMap.put(type.getId(), type);
        nameMap.put(type.getName(), type);
    }

    public static EntityType get(int id){
        return idMap.get(id);
    }

    public static EntityType get(String name){
        return nameMap.get(name);
    }

    public static int toId(String name){
        EntityType t = get(name);
        if (t != null) return t.id;
        return -1;
    }

    public static String toName(int id){
        EntityType t = get(id);
        if (t != null) return t.name;
        return null;
    }

    public static void lock(){
        locked = true;
    }

    public EntityType create(int id, String name) {
        if(locked) throw new IllegalStateException("already locked, please register using mods before server loaded! ");
        return new EntityType(id, name);
    }

    // ====

    @Getter
    private final int id;

    @Getter
    private final String name;

    private EntityType(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
