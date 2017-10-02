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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

@EnableAutoConfiguration
@EnableScheduling
@Configuration
public class Config {
    private static final int readTimeout = 15000;
    private static final int connTimeout = 60000;
    private static String password;
    private URL kontaktregisterUrl;
    private URL statisticsIngestUrl;

    @Autowired
    public Config(Environment environment) throws IOException {
        kontaktregisterUrl = environment.getRequiredProperty("url.base.kontaktregister", URL.class);
        statisticsIngestUrl = environment.getRequiredProperty("url.base.ingest.statistikk", URL.class);
        password = new String(Files.readAllBytes(Paths.get(environment.getRequiredProperty("file.base.difi-statistikk"))));
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
        return new IngestClient(statisticsIngestUrl, readTimeout, connTimeout, kontaktregister.owner(), kontaktregister.owner(), password);
    }

    @Bean
    public StatisticsMapper statisticsMapper() {
        return new StatisticsMapper();
    }
}
