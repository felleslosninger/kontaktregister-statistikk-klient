package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.controller.HelloWorldController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@Configuration
public class Config {
    @Bean
    public HelloWorldController restController() {
        return new HelloWorldController();
    }
}
