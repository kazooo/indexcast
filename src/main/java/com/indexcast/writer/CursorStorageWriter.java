package com.indexcast.writer;

import com.indexcast.component.Pair;
import com.indexcast.component.CursorMarkGlobalStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * This writer stores fetched cursor and docs-to-migrate number into cursor mark storage.
 *
 * @author Aleksei Ermak
 */

@Slf4j
@AllArgsConstructor
public class CursorStorageWriter implements ItemWriter<Pair<String, Integer>> {

    private final CursorMarkGlobalStorage cursorStorage;

    @Override
    public void write(List<? extends Pair<String, Integer>> items) {
        for (Pair<String, Integer> i : items) {
            log.debug("[cursor-writer][store] " + i.getKey());
            cursorStorage.addCursorAndObjNum(i.getKey(), i.getValue());
        }
    }
}
