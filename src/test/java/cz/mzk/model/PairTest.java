package cz.mzk.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PairTest {

    private Pair<String, Integer> pair;

    @Before
    public void init() {
        pair = new Pair<>("cursor", 10);
    }

    @Test
    public void testGetKeyAndValue() {
        assertEquals(pair.getKey(), "cursor");
        assertEquals((int) pair.getValue(), 10);
    }

    @Test
    public void testEqual() {
        assertEquals(pair, new Pair<>("cursor", 10));
    }
}
