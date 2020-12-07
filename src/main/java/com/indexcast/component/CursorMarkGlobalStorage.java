package com.indexcast.component;

import com.indexcast.configuration.IndexcastParameterConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


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
     * If a thread is already in the safe zone, other threads wait for it to be released.
     * If there is no more cursor after zone release and storage is closed, returns null.
     *
     * @return  cursor mark and docs-to-migrate number
     */
    public Pair<String, Integer> getNextCursorAndObjNum() {
        Pair<String, Integer> cursorWithMaxObj = null;
        while (cursorWithMaxObj == null && (!isClosed() || !cursorMarksWithObjectsCount.isEmpty())) {
            try {
                // returns null if storage can't give cursor mark within 10 seconds
                cursorWithMaxObj = cursorMarksWithObjectsCount.poll(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Caught InterruptedException while trying to take a cursor mark from the storage!");
            }
        }
        if (cursorWithMaxObj != null) {
            log.debug("[return] " + cursorWithMaxObj.getKey());
        }
        return cursorWithMaxObj;
    }

    public void close() {
        noMoreCursors = true;
    }

    public boolean isClosed() {
        log.debug("[storage is closed] " + noMoreCursors);
        return noMoreCursors;
    }
}
