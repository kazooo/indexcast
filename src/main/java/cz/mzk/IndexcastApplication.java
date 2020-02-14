package cz.mzk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


/**
 * @author Aleksei Ermak
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IndexcastApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndexcastApplication.class, args);
    }
}
