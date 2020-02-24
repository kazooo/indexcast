package cz.mzk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


/**
 * Indexcast application start point.
 * Exclude jdbc.DataSourceAutoConfiguration to run Spring Batch application without database usage.
 *
 * @author Aleksei Ermak
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IndexcastApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndexcastApplication.class, args);
    }
}
