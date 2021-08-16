package no.difi.kontaktregister.statistics;

import no.difi.statistics.ingest.client.IngestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class Config {

    private final Properties properties;

    @Autowired
    public Config(Properties properties) {
        this.properties = properties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public IngestClient ingestClient() {
        return new IngestClient(properties.getStatisticsIngestUrl(), properties.getReadTimeout(), properties.getConnTimeout(), properties.getReportOwner());
    }
}
