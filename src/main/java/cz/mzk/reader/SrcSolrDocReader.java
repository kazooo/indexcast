package cz.mzk.reader;

import cz.mzk.model.CursorMarkGlobalStorage;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemReader;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class SrcSolrDocReader implements ItemReader<List<SolrInputDocument>> {

    private CursorMarkGlobalStorage cursorMarkStorage;

    public SrcSolrDocReader(CursorMarkGlobalStorage cursorMarkStorage) {
        this.cursorMarkStorage = cursorMarkStorage;
    }

    @Override
    public List<SolrInputDocument> read() {
        return null;
    }
}
