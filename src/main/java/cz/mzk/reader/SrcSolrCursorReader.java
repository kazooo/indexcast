package cz.mzk.reader;

import javafx.util.Pair;
import org.springframework.batch.item.ItemReader;


/**
 * @author Aleksei Ermak
 */

public class SrcSolrCursorReader implements ItemReader<Pair<String, Integer>> {

    @Override
    public Pair<String, Integer> read() {
        return null;
    }
}
