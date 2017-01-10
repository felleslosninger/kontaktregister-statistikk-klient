package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.fetch.service.LastDatapoint;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.kontaktregister.statistics.schedule.KontaktregisterScheduler;
import no.difi.kontaktregister.statistics.transfer.DataTransfer;
import no.difi.statistics.ingest.client.IngestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

@EnableAutoConfiguration
@EnableScheduling
@Configuration
public class Config {
    public static final String owner = "991825827";
    private static final String password = "c:uHW%m'7A2Uuo-~,lx~";
    private static final int readTimeout = 5000;
    private static final int connTimeout = 15000;
    private final URL kontaktregisterUrl;
    private final URL statisticsIngestUrlString;

    @Autowired
    public Config(Environment environment) {
        try {
            kontaktregisterUrl = environment.getRequiredProperty("url.base.kontaktregister", URL.class);
            statisticsIngestUrlString = environment.getRequiredProperty("url.base.ingest.statistikk", URL.class);
        } catch (IllegalStateException e) {
            throw new ArgumentMissing("Missing argument. url.base.kontaktregister, url.base.ingest.statistikk and url.base.query.statistikk are required", e);
        }
    }

    @Bean
    public KontaktregisterScheduler kontaktregisterScheduler() {
        return new KontaktregisterScheduler(dataTransfer(), lastDatapoint());
    }

    @Bean
    public DataTransfer dataTransfer() {
        return new DataTransfer(kontaktregisterFetch(), konktaktregisterPush(), statisticsMapper());
    }

    @Bean
    public KontaktregisterFetch kontaktregisterFetch() {
        return new KontaktregisterFetch(kontaktregisterUrl, restTemplate());
    }

    @Bean
    public LastDatapoint lastDatapoint() {
        return new LastDatapoint(ingestClient());
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
        return new IngestClient(statisticsIngestUrlString, readTimeout, connTimeout, owner, owner, password);
    }

    @Bean
    public StatisticsMapper statisticsMapper() {
        return new StatisticsMapper();
    }
}
