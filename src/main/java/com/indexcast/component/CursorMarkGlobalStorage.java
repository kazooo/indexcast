package com.indexcast.component;

import com.indexcast.configuration.IndexcastParameterConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * This class is used as a global synchronized storage for cursor marks
 * and document numbers for migration from this marks.
 * Closes when has no marks, announcing the end of the migration.
 *
 * @author Aleksei Ermak
 */

@Component
@Slf4j
public class CursorMarkGlobalStorage {

    private boolean noMoreCursors;
    private final BlockingQueue<Pair<String, Integer>> cursorMarksWithObjectsCount;

    public CursorMarkGlobalStorage(@Value("#{indexcastParameterConfiguration.storageSize}") int storageSize) {
        cursorMarksWithObjectsCount = new LinkedBlockingDeque<>(storageSize);
        noMoreCursors = false;
    }

    /**
     * Stores new cursor mark with docs-to-migrate number.
     *
     * @param cursorMark  new cursor mark
     * @param objNum      how many documents can be read from given cursor mark
     */
    public void addCursorAndObjNum(String cursorMark, Integer objNum) {
        while (true) {
            try {
                cursorMarksWithObjectsCount.put(new Pair<>(cursorMark, objNum));
                log.debug("[store] " + cursorMark);
                break;
            } catch (InterruptedException e) {
                log.warn("Catch InterruptedException when trying to put new cursor mark to the storage!");
            }
        }
    }

    /**
     * Retrieves cursor mark and docs-to-migrate number from storage.
     * If any thread already in a safe zone, another threads wait for it release.
     * If after zone release there is no more cursors and storage is closed, returns null.
     *
     * @return  cursor mark and docs-to-migrate number
     */
    public Pair<String, Integer> getNextCursorAndObjNum() {
        while (!isClosed() || !cursorMarksWithObjectsCount.isEmpty()) {
            try {
                Pair<String, Integer> cursorWithMaxObj = cursorMarksWithObjectsCount.take();
                log.debug("[return] " + cursorWithMaxObj.getKey());
                return cursorWithMaxObj;
            } catch (InterruptedException e) {
                log.warn("Catch InterruptedException when trying to take a cursor mark from the storage!");
            }
        }
        return null;
    }

    public void close() {
        noMoreCursors = true;
    }

    public boolean isClosed() {
        log.debug("[storage is closed] " + noMoreCursors);
        return noMoreCursors;
    }
}
