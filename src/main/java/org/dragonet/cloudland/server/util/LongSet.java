package org.dragonet.cloudland.server.util;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created on 2017/1/11.
 */
public class LongSet {

    private final boolean concurrent;

    private Map<Integer, Set<Integer>> map;

    public LongSet(boolean concurrent) {
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

    public boolean contains(int v1, int v2) {
        if(!map.containsKey(v1)) return false;
        return map.get(v1).contains(v2);
    }

    public void add(int v1, int v2) {
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

    public void remove(int v1, int v2) {
        if(!map.containsKey(v1)) return;
        map.get(v1).remove(v2);
        if(map.get(v1).size() <= 0) {
            map.remove(v1);
        }
    }

    public void forEach(Consumer<int[]> action) {
        int[] v = new int[2];
        map.entrySet().forEach((e) -> {
                    v[0] = e.getKey();
                    e.getValue().forEach((vv) -> {
                        v[1] = vv;
                        action.accept(v);
                    });
                }
        );
    }
}
