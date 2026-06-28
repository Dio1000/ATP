package me.dariansandru.domain.data_structures.other;

import me.dariansandru.domain.data_structures.other.exceptions.BiMapException;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * The BiMap (Bijective Map) is a data structure that closely
 * resembles a usual map but with the additional property that
 * the key can also be retrieved from the value, not just the value
 * form the key. Thus, the following property is true:
 * map[key] = value AND map[value] = key.
 */
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

    public Object getOrDefault(Object key, Object def) {
        return map.getOrDefault(key, def);
    }

    public boolean has(Object key) {
        return map.containsKey(key);
    }

    public void reset() {
        map.clear();
    }

}