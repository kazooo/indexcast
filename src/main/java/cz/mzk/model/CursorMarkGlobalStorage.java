package cz.mzk.model;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * @author Aleksei Ermak
 */

@Component
public class CursorMarkGlobalStorage {

    private boolean noMoreCursors;
    private final List<Pair<String, Integer>> cursorMarksWithObjectsCount;
    private final Logger logger = LoggerFactory.getLogger(CursorMarkGlobalStorage.class);

    CursorMarkGlobalStorage() {
        noMoreCursors = false;
        cursorMarksWithObjectsCount = Collections.synchronizedList(new ArrayList<>());
    }

    public void addCursorAndObjNum(String cursorMark, Integer objNum) {
        synchronized (cursorMarksWithObjectsCount) {
            logger.debug("[store] " + cursorMark);
            cursorMarksWithObjectsCount.add(new Pair<>(cursorMark, objNum));
        }
    }

    public synchronized Pair<String, Integer> getNextCursorAndObjNum() {
        while (cursorMarksWithObjectsCount.isEmpty() && !isClosed()) {
            waitForCursor();
        }
        if (isClosed()) {
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
        return noMoreCursors;
    }

    private void waitForCursor() {
        try {
            Thread.sleep(3000); // sleep 3 sec
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
