package cz.mzk.writer;

import cz.mzk.model.CursorMarkGlobalStorage;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class CursorStorageWriter implements ItemWriter<Pair<String, Integer>> {

    CursorMarkGlobalStorage cursorStorage;
    private final Logger logger = LoggerFactory.getLogger(CursorStorageWriter.class);

    public CursorStorageWriter(CursorMarkGlobalStorage storage) {
        cursorStorage = storage;
    }

    @Override
    public void write(List<? extends Pair<String, Integer>> items) {
        for (Pair<String, Integer> i : items) {
            logger.debug("[write] " + i.getKey());
            cursorStorage.addCursorAndObjNum(i.getKey(), i.getValue());
        }
    }
}
