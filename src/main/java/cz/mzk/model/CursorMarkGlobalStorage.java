package cz.mzk.model;

import javafx.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * @author Aleksei Ermak
 */

@Component
public class CursorMarkGlobalStorage {

    final List<Pair<String, Integer>> cursorMarksWithObjectsCount;

    CursorMarkGlobalStorage() {
        cursorMarksWithObjectsCount = Collections.synchronizedList(new ArrayList<>());
    }

    public void addCursorAndObjNum(String cursorMark, Integer objNum) {
        synchronized (cursorMarksWithObjectsCount) {
            cursorMarksWithObjectsCount.add(new Pair<>(cursorMark, objNum));
        }
    }

    public Pair<String, Integer> getNextCursorAndObjNum() {
        synchronized (cursorMarksWithObjectsCount) {
            return cursorMarksWithObjectsCount.remove(0);
        }
    }
}
