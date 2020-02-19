package cz.mzk.writer;

import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.component.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * This writer stores fetched cursor and numFound numbers into cursor mark storage.
 *
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
            logger.info("[cursor-writer][write] " + i.getKey());
            cursorStorage.addCursorAndObjNum(i.getKey(), i.getValue());
        }
    }
}
