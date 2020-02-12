package cz.mzk.reader;

import cz.mzk.model.CursorMarkGlobalStorage;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.batch.item.ItemReader;


/**
 * @author Aleksei Ermak
 */

public class SrcSolrDocReader implements ItemReader<SolrDocumentList> {

    private CursorMarkGlobalStorage cursorMarkStorage;

    public SrcSolrDocReader(CursorMarkGlobalStorage cursorMarkStorage) {
        this.cursorMarkStorage = cursorMarkStorage;
    }

    @Override
    public SolrDocumentList read() {
        return null;
    }
}
