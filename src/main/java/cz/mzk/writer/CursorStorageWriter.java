package cz.mzk.writer;

import cz.mzk.model.CursorMarkGlobalStorage;
import javafx.util.Pair;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class CursorStorageWriter implements ItemWriter<Pair<String, Integer>> {

    private CursorMarkGlobalStorage cursorMarkStorage;

    public CursorStorageWriter(CursorMarkGlobalStorage cursorMarkStorage) {
        this.cursorMarkStorage = cursorMarkStorage;
    }

    @Override
    public void write(List<? extends Pair<String, Integer>> items) {

    }
}
