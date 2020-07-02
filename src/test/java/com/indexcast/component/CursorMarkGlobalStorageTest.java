package com.indexcast.component;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author Aleksei Ermak
 */

public class CursorMarkGlobalStorageTest {

    private CursorMarkGlobalStorage storage;

    @Before
    public void setup() {
        storage = new CursorMarkGlobalStorage(2);
    }

    @Test
    public void testReadWrite() {
        String cursor = "cursor1";
        Integer maxNumObj = 1000;
        storage.addCursorAndObjNum(cursor, maxNumObj);
        Pair<String, Integer> cursorFromStorage = storage.getNextCursorAndObjNum();
        assertEquals(cursor, cursorFromStorage.getKey());
        assertEquals(maxNumObj, cursorFromStorage.getValue());
    }

    @Test
    public void testWaitingForFilling() throws InterruptedException {
        Thread t = new Thread(() -> {
            storage.addCursorAndObjNum("cursor", 1);
            storage.addCursorAndObjNum("cursor", 1);
            storage.addCursorAndObjNum("cursor", 1);
        });
        t.start();
        Thread.sleep(4000);
        assertTrue(t.isAlive());
        Thread.sleep(1000);
        t.interrupt();
    }

    @Test
    public void testWhenStorageIsClosed() {
        storage.close();
        assertTrue(storage.isClosed());
        assertNull(storage.getNextCursorAndObjNum());
    }
}
