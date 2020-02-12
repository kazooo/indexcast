package cz.mzk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


/**
 * @author Aleksei Ermak
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Solr2SolrApplication {

    public static void main(String[] args) {
        SpringApplication.run(Solr2SolrApplication.class, args);
    }
}
