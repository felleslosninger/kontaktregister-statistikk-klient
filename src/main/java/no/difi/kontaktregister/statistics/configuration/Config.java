package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.kontaktregister.statistics.schedule.KontaktregisterScheduler;
import no.difi.kontaktregister.statistics.util.ReadSecret;
import no.difi.kontaktregister.statistics.util.StatisticsReportType;
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

import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

@EnableAutoConfiguration
@EnableScheduling
@Configuration
public class Config {
    private static final int readTimeout = 5000;
    private static final int connTimeout = 15000;
    private static String baseUrl;
    private static String password;

    @Autowired
    public Config(Environment environment) {
        try {

            password = ReadSecret.getPwd(environment.getRequiredProperty("file.base.difi-statistikk"));
            baseUrl = "url.base.statistikk";
            new URL(environment.getRequiredProperty("url.base.kontaktregister"));
            new URL(environment.getRequiredProperty(baseUrl));
        } catch (IllegalStateException e) {
            throw new ArgumentMissing("One or more of the required arguments is missing. Check with documentation which are required.", e);
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

        return new IngestClient(baseUrl, readTimeout, connTimeout, kontaktregister.owner(), kontaktregister.owner(), password);
    }

    @Bean
    public StatisticsMapper statisticsMapper() {
        return new StatisticsMapper();
    }
}
