package no.difi.kontaktregister.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class Properties {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZonedDateTime baseLine;


    public Properties(Environment environment) {
        baseLine = getBaseLine(environment.getProperty("statistics.years.back", Integer.class));
    }

    protected ZonedDateTime getBaseLine(Integer yearsFromConfig) {
        int yearsOfStatistics = 3;
        logger.info("Found setting for statistics.years.back in enviroment. Fetching data: " + yearsFromConfig + " years back. Default years if null: " + yearsOfStatistics);
        yearsOfStatistics = yearsFromConfig != null ? yearsFromConfig : yearsOfStatistics;
        return ZonedDateTime.now().minusYears(yearsOfStatistics).truncatedTo(ChronoUnit.HOURS);
    }


    public ZonedDateTime getBaseLine() {
        return baseLine;
    }


}
