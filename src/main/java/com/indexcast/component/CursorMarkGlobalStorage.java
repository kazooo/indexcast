package com.indexcast.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * This class is used as a global synchronized storage for cursor marks
 * and document numbers for migration from this marks. Closes when has no marks, announcing the end of the migration.
 *
 * @author Aleksei Ermak
 */

@Component
public class CursorMarkGlobalStorage {

    private boolean noMoreCursors;
    private final List<Pair<String, Integer>> cursorMarksWithObjectsCount;
    private final Logger logger = LoggerFactory.getLogger(CursorMarkGlobalStorage.class);

    public CursorMarkGlobalStorage() {
        noMoreCursors = false;
        cursorMarksWithObjectsCount = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Stores new cursor mark with docs-to-migrate number.
     *
     * @param cursorMark  new cursor mark
     * @param objNum      how many documents can be read from given cursor mark
     */
    public void addCursorAndObjNum(String cursorMark, Integer objNum) {
        synchronized (cursorMarksWithObjectsCount) {
            logger.debug("[store] " + cursorMark);
            cursorMarksWithObjectsCount.add(new Pair<>(cursorMark, objNum));
        }
    }

    /**
     * Retrieves cursor mark and docs-to-migrate number from storage.
     * If any thread already in a safe zone, another threads wait for it release.
     * If after zone release there is no more cursors and storage is closed, returns null.
     *
     * @return  cursor mark and docs-to-migrate number
     */
    public synchronized Pair<String, Integer> getNextCursorAndObjNum() {
        while (cursorMarksWithObjectsCount.isEmpty() && !isClosed()) {
            waitForCursor();
        }
        if (isClosed() && cursorMarksWithObjectsCount.isEmpty()) {
            return null;
        }
        Pair<String, Integer> cursorWithMaxObj = cursorMarksWithObjectsCount.remove(0);
        logger.debug("[return] " + cursorWithMaxObj.getKey());
        return cursorWithMaxObj;
    }

    public void close() {
        noMoreCursors = true;
    }

    public boolean isClosed() {
        logger.debug("[storage is closed] " + noMoreCursors);
        return noMoreCursors;
    }

    /**
     * Make thread wait 3 seconds before it can check storage again.
     */
    private void waitForCursor() {
        try {
            Thread.sleep(3000); // sleep 3 sec
        } catch (InterruptedException e) {
            logger.warn("Waiting for a cursor mark failed...");
        }
    }
}
