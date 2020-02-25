package com.indexcast.writer;

import com.indexcast.component.Pair;
import com.indexcast.component.CursorMarkGlobalStorage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CursorStorageWriterTest {

    @Test
    public void testWrite() {
        CursorMarkGlobalStorage storage = new CursorMarkGlobalStorage();
        CursorStorageWriter writer = new CursorStorageWriter(storage);
        List<Pair<String, Integer>> cursors = Arrays.asList(
                new Pair<>("cursor1", 500),
                new Pair<>("cursor2", 1000),
                new Pair<>("cursor3", 1500),
                new Pair<>("cursor4", 2000)
        );
        writer.write(cursors);
        for (Pair<String, Integer> cursor : cursors) {
            Pair<String, Integer> cursorFromStorage = storage.getNextCursorAndObjNum();
            assertEquals(cursor, cursorFromStorage);
        }
    }
}
