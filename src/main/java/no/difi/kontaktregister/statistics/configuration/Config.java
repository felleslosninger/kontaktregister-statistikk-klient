package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.kontaktregister.statistics.schedule.KontaktregisterScheduler;
import no.difi.statistics.ingest.client.IngestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

@EnableAutoConfiguration
@EnableScheduling
@Configuration
public class Config {
    private static final String owner = "991825827";
    private static final String base_url = "https://test-statistikk-inndata.difi.no";
    private static final String password = "q-^Z-FepFb>{%~p/Y42k";
    private static final int readTimeout = 5000;
    private static final int connTimeout = 15000;

    @Autowired
    public Config(Environment environment) {
        try {
            new URL(environment.getRequiredProperty("url.base.kontaktregister"));
            new URL(environment.getRequiredProperty("url.base.statistikk"));
        } catch (IllegalStateException e) {
            throw new ArgumentMissing("Missing argument. url.base.kontaktregister and url.base.url.base.statistikk is required", e);
        } catch (MalformedURLException e) {
            throw new ArgumentMissing("url.base.kontaktregister and/or url.base.url.base.statistikk not valid URL", e);
        }
    }

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
        return new IngestClient(base_url, readTimeout, connTimeout, owner, owner, password);
    }

    @Bean
    public StatisticsMapper statisticsMapper() {
        return new StatisticsMapper();
    }
}
