package me.dariansandru.domain.data_structures;

import me.dariansandru.domain.data_structures.exceptions.BiMapException;

import java.util.HashMap;
import java.util.Map;

public class BiMap {

    public Map<Object, Object> map;

    public BiMap() {
        map = new HashMap<>();
    }

    public void put(Object key, Object value) {
        map.put(key, value);
        map.put(value, key);
    }

    public Object get(Object key) {
        try {
            return map.get(key);
        }
        catch (IllegalStateException e) {
            throw new BiMapException("No such element in Bijective Map!", e);
        }
    }

    public boolean has(Object key) {
        return map.containsKey(key);
    }

}