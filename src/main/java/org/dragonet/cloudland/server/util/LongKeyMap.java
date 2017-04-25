package org.dragonet.cloudland.server.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created on 2017/1/11.
 */
public class LongKeyMap<V> {
    private final boolean concurrent;

    private final Map<Integer, Map<Integer, V>> map;

    public LongKeyMap(boolean concurrent) {
        this.concurrent = concurrent;
        Map<Integer, Map<Integer, V>> m = new HashMap<>();
        if(concurrent){
            map = Collections.synchronizedMap(m);
        } else {
            map = m;
        }
    }

    public V get(int k1, int k2) {
        if(!map.containsKey(k1)) return null;
        return map.get(k1).get(k2);
    }

    public boolean containsKey(int k1, int k2) {
        if(!map.containsKey(k1)) return false;
        return map.get(k1).containsKey(k2);
    }

    public void put(int k1, int k2, V value) {
        Map<Integer, V> l2Map = map.get(k1);
        if(l2Map == null){
            Map<Integer, V> newL2Map = new HashMap<>();
            if(concurrent) {
                l2Map = Collections.synchronizedMap(newL2Map);
            } else {
                l2Map = newL2Map;
            }
            map.put(k1, l2Map);
        }
        l2Map.put(k2, value);
    }

    public void remove(int k1, int k2){
        if(!map.containsKey(k1)) return;
        Map<Integer, V> l2Map = map.get(k1);
        if(l2Map.containsKey(k2)){
            l2Map.remove(k2);
            if(l2Map.size() == 0) {
                map.remove(k1);
            }
        }
    }

    public void forEachValue(Consumer<V> action) {
        map.values().forEach((l2) -> {
            l2.values().forEach((v) -> {
                action.accept(v);
            });
        });
    }
}
