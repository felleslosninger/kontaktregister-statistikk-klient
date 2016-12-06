package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.kontaktregister.statistics.schedule.KontaktregisterScheduler;
import no.difi.statistics.ingest.client.IngestClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableAutoConfiguration
@EnableScheduling
@Configuration
public class Config {
    private static final String owner = "991825827";
    private static final String base_url = "https://test-statistikk-inndata.difi.no";
    private static final String password = "K.b4f=3Bf~ge{$-rDp]p";

    @Bean
    public KontaktregisterScheduler kontaktregisterScheduler() {
        return new KontaktregisterScheduler(kontaktregisterFetch(), konktaktregisterPush(), statisticsMapper());
    }

    @Bean
    public KontaktregisterFetch kontaktregisterFetch() {
        return new KontaktregisterFetch(restTemplate());
    }

    @Bean
    public KontaktregisterPush konktaktregisterPush() {
        return new KontaktregisterPush(ingestClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public IngestClient ingestClient() {
        return new IngestClient(base_url, owner, owner, password);
    }

    @Bean
    public StatisticsMapper statisticsMapper() {
        return new StatisticsMapper();
    }
}
