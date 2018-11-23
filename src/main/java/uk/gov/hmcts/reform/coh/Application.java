package uk.gov.hmcts.reform.coh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Clock;
import java.util.TimeZone;
import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableSwagger2
@EnableAutoConfiguration
@EnableScheduling
public class Application {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
