package cz.mzk.configuration;

import cz.mzk.solr.DstSolrClient;
import cz.mzk.solr.SrcSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
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
        "CORE_NAME=solr/test_core", //
        "SRC_SOLR_HOST=no_host",    // unnecessary properties, only for parameter configuration filling
        "DST_SOLR_HOST=no_host",    //
        "SCHEMA_PATH=src/test/resources/migration-test-schema.yml"
})
public class WorkFlowIntegrationTest {

    @Test
    public void testApplicationWorkResults() throws IOException, SolrServerException {
        int dstNumFound = (int) SolrServerClientTestContextConfiguration.dstSolrServer
                .query(new SolrQuery("*:*")).getResults().getNumFound();
        assertEquals(100, dstNumFound);
    }

    @TestConfiguration
    static class SolrServerClientTestContextConfiguration {

        static EmbeddedSolrServer srcSolrServer;
        static EmbeddedSolrServer dstSolrServer;
        static String coreName = "test_core";
        static int docNum = 100;

        @Bean
        public DstSolrClient dstSolrClient() throws IOException, SolrServerException {
            CoreContainer dstContainer = new CoreContainer("src/test/resources/dst_solr");
            dstContainer.load();
            dstSolrServer = new EmbeddedSolrServer(dstContainer, coreName);

            dstSolrServer.deleteByQuery("*:*");
            dstSolrServer.commit();

            DstSolrClient client = new DstSolrClient("no_host", "test_core");
            client.setupCustomSolrClient(dstSolrServer);
            return client;
        }

        @Bean
        public SrcSolrClient srcSolrClient() throws IOException, SolrServerException {
            CoreContainer srcContainer = new CoreContainer("src/test/resources/src_solr");
            srcContainer.load();
            srcSolrServer = new EmbeddedSolrServer(srcContainer, coreName);

            srcSolrServer.deleteByQuery("*:*");
            srcSolrServer.commit();
            fillSrcSolr(docNum);

            SrcSolrClient client = new SrcSolrClient("no_host", "test_core");
            client.setupCustomSolrClient(srcSolrServer);
            return client;
        }

        private void fillSrcSolr(int docNum) throws IOException, SolrServerException {
            for (int i = 0; i < docNum; i++) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", generateRandomAlphanumericString());
                doc.addField("title", generateRandomAlphanumericString());
                srcSolrServer.add(doc);
            }
            srcSolrServer.commit();
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
