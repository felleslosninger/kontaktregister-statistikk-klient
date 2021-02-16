package no.difi.kontaktregister.statistics;

import no.difi.statistics.ingest.client.IngestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private Environment environment;

    @Bean
    public Properties properties() {
        return new Properties(environment);
    }

    @Bean
    public RestTemplate kontaktRegisterRestTemplate(Environment environment) {
        return new RestTemplateBuilder().rootUri(environment.getRequiredProperty("url.base.kontaktregister")).build();
    }

    @Bean
    public IngestClient ingestClient(Environment environment) throws IOException {
        return new IngestClient(
                environment.getRequiredProperty("url.base.ingest.statistikk", URL.class),
                15000,
                60000,
                kontaktregister.owner(),
                kontaktregister.owner(),
                new String(Files.readAllBytes(Paths.get(environment.getRequiredProperty("file.base.difi-statistikk"))))
        );
    }

}
