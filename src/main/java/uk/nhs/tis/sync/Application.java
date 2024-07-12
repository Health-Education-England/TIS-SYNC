package uk.nhs.tis.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import uk.nhs.tis.sync.config.ApplicationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@ComponentScan(basePackages = {"com.transformuk.hee.tis.tcs",
    "uk.nhs.tis.sync", "com.transformuk.hee.tis.reference.client",
    "com.transformuk.hee.tis.profile.client"})
@EnableWebMvc
@EnableSpringDataWebSupport
@PropertySource({"classpath:/config/application.properties",
    "classpath:/config/referenceclientapplication.properties",
    "classpath:/config/profileclientapplication.properties",
    "classpath:/config/tcsclientapplication.properties"
})
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableAutoConfiguration()
public class Application {
  private static final Logger log = LoggerFactory.getLogger(Application.class);

  private final Environment env;

  public Application(Environment env) {
    this.env = env;
  }

  public static void main(String[] args) throws UnknownHostException {
    SpringApplication app = new SpringApplication(Application.class);
    Environment env = app.run(args).getEnvironment();
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    log.info("\n----------------------------------------------------------\n\t" +
            "Application '{}' is running! Access URLs:\n\t" +
            "Local: \t\t{}://localhost:{}\n\t" +
            "External: \t{}://{}:{}\n\t" +
            "Profile(s): \t{}\n----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        protocol,
        env.getProperty("server.port"),
        protocol,
        InetAddress.getLocalHost().getHostAddress(),
        env.getProperty("server.port"),
        env.getActiveProfiles());
  }

}
