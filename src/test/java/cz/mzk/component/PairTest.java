package cz.mzk.component;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * @author Aleksei Ermak
 */

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
        assertEquals(pair, pair);
        assertEquals(pair, new Pair<>("cursor", 10));
    }

    @Test
    public void testNotEqual() {
        assertNotEquals(pair, new Pair<>("cursor", 0));
        assertNotEquals(pair, new Pair<>("*", 10));
        assertNotEquals(pair, new ArrayList<>());
    }
}
