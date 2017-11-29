package no.difi.kontaktregister.statistics.schedule;

import no.difi.kontaktregister.statistics.fetch.service.LastDatapoint;
import no.difi.kontaktregister.statistics.transfer.DataTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.YEARS;
import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

public class KontaktregisterScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String cron_one_minute_interval = "0 */1 * * * *";

    private final DataTransfer dataTransfer;
    private final LastDatapoint lastDatapoint;

    public KontaktregisterScheduler(DataTransfer dataTransfer, LastDatapoint lastDatapoint) {
        this.dataTransfer = dataTransfer;
        this.lastDatapoint = lastDatapoint;
    }

    @Scheduled(cron = cron_one_minute_interval)
    public void fetchKontaktregisterReportData() {
        ZonedDateTime from = lastDatapoint.get(kontaktregister.seriesId());
        ZonedDateTime to = ZonedDateTime.now(ZoneId.of("UTC"));
        if (isMoreThanOneYearDifference(from, to))
            to = oneYearAfter(from);
        logger.info("Transfering data from {} to {}", from, to);
        try {
            dataTransfer.transfer(from, to);
        } catch (Exception e) {
            logger.error("Failed to transfer data", e);
        }
        logger.info("Data transfer completed");
    }

    private ZonedDateTime oneYearAfter(ZonedDateTime reference) {
        return reference.plus(1, YEARS);
    }

    private boolean isMoreThanOneYearDifference(ZonedDateTime from, ZonedDateTime to) {
        return from.plus(1, YEARS).isBefore(to);
    }

}
