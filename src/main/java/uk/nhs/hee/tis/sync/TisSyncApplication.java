package uk.nhs.hee.tis.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@SpringBootApplication
@EnableWebMvc
@EnableSpringDataWebSupport
@PropertySource(
    {
        "classpath:/application.properties",
        //"classpath:/config/profileclientapplication.properties",
        //"classpath:/config/referenceclientapplication.properties",
        //"classpath:/config/tcsclientapplication.properties"
    }
)
public class TisSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(TisSyncApplication.class, args);
	}

}