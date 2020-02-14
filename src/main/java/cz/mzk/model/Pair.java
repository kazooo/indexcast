package cz.mzk.model;

public class Pair<K,V> {

    private K key;
    private V value;

    public Pair(K k, V v) {
        key = k;
        value = v;
    }

    public K getKey() { return key; }

    public V getValue() { return value; }
}
