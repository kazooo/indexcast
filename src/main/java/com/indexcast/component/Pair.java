package com.indexcast.component;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


/**
 * This class is used in project as auxiliary structure.
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Pair<K,V> {
    private final K key;
    private final V value;
}
