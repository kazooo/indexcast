package com.indexcast.configuration;

import com.indexcast.solr.SrcSolrClient;
import com.indexcast.solr.DstSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
        "THREADS=3",
        "QUERY=*:*",
        "PER_CYCLE=10",
        "SRC_CORE_NAME=solr/test_core", //
        "DST_CORE_NAME=solr/test_core", //
        "SRC_SOLR_HOST=no_host",        // unnecessary properties,
        "DST_SOLR_HOST=no_host",        // only for parameter configuration filling
        "WAIT_IF_SOLR_FAIL=500",        //
        "SCHEMA_PATH=src/test/resources/migration-test-schema.yml"
})
public class ApplicationSystemTest {

    private static UnstableSolrServer srcSolrServer;
    private static UnstableSolrServer dstSolrServer;
    private static final String srcCoreName = "test_src_core";
    private static final String dstCoreName = "test_dst_core";
    private static final int waitIfSolrFail = 500;
    private static final int docNum = 321;

    @Test
    public void testApplicationWorkResults() throws IOException, SolrServerException {
        dstSolrServer.setAvoidException(true);
        int dstNumFound = (int) dstSolrServer.query(new SolrQuery("*:*")).getResults().getNumFound();
        assertEquals(docNum, dstNumFound);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoDeletedDocs() throws IOException, SolrServerException {
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = request.process(dstSolrServer);
        NamedList<Object> coreStatus = cores.getCoreStatus(dstCoreName);
        SimpleOrderedMap<Integer> indexStatus = (SimpleOrderedMap<Integer>) coreStatus.get("index");
        int deletedDocs = indexStatus.get("deletedDocs");
        assertEquals(0, deletedDocs);
    }

    @TestConfiguration
    static class SolrServerClientTestContextConfiguration {

        @Bean
        public SrcSolrClient srcSolrClient() throws IOException, SolrServerException {
            CoreContainer srcContainer = new CoreContainer("src/test/resources/src_solr");
            srcContainer.load();
            srcSolrServer = new UnstableSolrServer(srcContainer, srcCoreName);

            srcSolrServer.deleteByQuery("*:*");
            srcSolrServer.commit();
            fillSrcSolr(docNum);

            return new SrcSolrClient(
                    srcSolrServer, "no_host", srcCoreName, waitIfSolrFail
            );
        }

        @Bean
        public DstSolrClient dstSolrClient() throws IOException, SolrServerException {
            CoreContainer dstContainer = new CoreContainer("src/test/resources/dst_solr");
            dstContainer.load();
            dstSolrServer = new UnstableSolrServer(dstContainer, dstCoreName);

            dstSolrServer.deleteByQuery("*:*");
            dstSolrServer.commit();

            return new DstSolrClient(
                    dstSolrServer, "no_host", dstCoreName, waitIfSolrFail
            );
        }

        private void fillSrcSolr(int docNum) throws IOException, SolrServerException {
            srcSolrServer.setAvoidException(true);
            for (int i = 0; i < docNum; i++) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", generateRandomAlphanumericString());
                doc.addField("title", generateRandomAlphanumericString());
                srcSolrServer.add(doc);
            }
            srcSolrServer.commit();
            srcSolrServer.setAvoidException(false);
        }

        public String generateRandomAlphanumericString() {
            int leftLimit = 48; // numeral '0'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            Random random = new Random();

            return random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
    }
}

class UnstableSolrServer extends EmbeddedSolrServer {

    private Random r;
    private int min;
    private int max;
    private boolean avoidException;

    public UnstableSolrServer(CoreContainer coreContainer, String coreName) {
        super(coreContainer, coreName);
        r = new Random();
        min = 0;
        max = 100;
    }

    @Override
    public QueryResponse query(String collection, SolrParams params) throws SolrServerException, IOException {
        if (doNormally() || avoidException) {
            return super.query(collection, params);
        } else {
            throw new SolrServerException("test exception");
        }
    }

    @Override
    public UpdateResponse add(String collection, SolrInputDocument doc) throws SolrServerException, IOException {
        if (doNormally() || avoidException) {
            return super.add(collection, doc);
        } else {
            throw new SolrServerException("test exception");
        }
    }

    public void setAvoidException(boolean avoid) {
        avoidException = avoid;
    }

    private boolean doNormally() {
        int randomInt = r.nextInt((max - min) + 1) + min;
        return randomInt > max/2;
    }
}
