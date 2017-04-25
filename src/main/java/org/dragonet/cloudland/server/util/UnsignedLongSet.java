package org.dragonet.cloudland.server.util;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created on 2017/1/11.
 */
public class UnsignedLongSet {

    private final boolean concurrent;

    private Map<Integer, Set<Integer>> map;

    public UnsignedLongSet(boolean concurrent) {
        this.concurrent = concurrent;
        Map<Integer, Set<Integer>> m = new HashMap<>();
        if(concurrent) {
            map = Collections.synchronizedMap(m);
        } else {
            map = m;
        }
    }

    public void clear(){
        map.clear();
    }

    public boolean contains(long value) {
        int k1 = (int)((value >> 32) & 0xFFFFFFFF);
        if(!map.containsKey(k1)) return false;
        int k2 = (int)(value & 0xFFFFFFFF);
        if(map.get(k1).contains(k2)) return true;
        return false;
    }

    public void add(long v) {
        int v1 = (int)(v >> 32);
        int v2 = (int)(v & 0xFFFFFFFF);
        Set<Integer> l2Set = map.get(v1);
        if(!map.containsKey(v1)) {
            if(l2Set == null) {
                Set<Integer> newL2Set = new HashSet<>();
                if(concurrent) {
                    l2Set = Collections.synchronizedSet(newL2Set);
                } else {
                    l2Set = newL2Set;
                }
                map.put(v1, l2Set);
            }
        }
        l2Set.add(v2);
    }

    public void remove(long value) {
        int k1 = (int)((value >> 32) & 0xFFFFFFFF);
        if(!map.containsKey(k1)) return;
        int k2 = (int)(value & 0xFFFFFFFF);
        map.get(k1).remove(k2);
    }

    public void forEach(Consumer<Long> action) {
        map.entrySet().forEach((e) -> {
                    e.getValue().forEach((vv) -> {
                        action.accept(((long)e.getKey()) << 32 | (long)vv);
                    });
                }
        );
    }
}
