package uk.gov.hmcts.reform.coh;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import javax.sql.DataSource;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private DataSource dataSource;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");
            System.out.println("DATASOURCE = " + dataSource.toString() + "Connection" + dataSource.getConnection());

            System.out.println("Saving....");
            OnlineHearing onlineHearing = new OnlineHearing();
            onlineHearing.setExternalRef("4539000034");
            OnlineHearing savedOnlineHearing = onlineHearingRepository.save(onlineHearing);
            System.out.println("Saved!");

            System.out.println("\n1.findAll()...");

            for (OnlineHearing onlineHearingOne : onlineHearingRepository.findAll()) {
                System.out.println(onlineHearingOne.toString());
            }

            System.out.println("Done!");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

        };
    }

}
