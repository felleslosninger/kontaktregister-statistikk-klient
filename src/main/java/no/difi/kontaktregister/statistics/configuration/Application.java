package no.difi.kontaktregister.statistics.configuration;

import org.springframework.boot.SpringApplication;

public class Application {
    public static void main(final String[] args) throws Exception {
        //Minimum two arguments are required for application to start
        if (args == null || args.length < 2) {
            throw new ArgumentMissing("Missing argument. url.base.kontaktregister, url.base.ingest.statistikk is required");
        }

        SpringApplication.run(Config.class, args);
    }
}
