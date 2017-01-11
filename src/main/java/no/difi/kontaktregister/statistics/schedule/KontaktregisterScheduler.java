package no.difi.kontaktregister.statistics.schedule;

import no.difi.kontaktregister.statistics.fetch.service.LastDatapoint;
import no.difi.kontaktregister.statistics.push.mapper.MapperError;
import no.difi.kontaktregister.statistics.transfer.DataTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;
import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

public class KontaktregisterScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String cron_one_minute_interval = "0 */1 * * * *";
    private static final String cron_ten_minutes_past_every_hour = "0 10 * * * *";

    private final DataTransfer dataTransfer;
    private final LastDatapoint lastDatapoint;

    public KontaktregisterScheduler(DataTransfer dataTransfer, LastDatapoint lastDatapoint) {
        this.dataTransfer = dataTransfer;
        this.lastDatapoint = lastDatapoint;
    }

    @Scheduled(cron = cron_ten_minutes_past_every_hour)
    public void fetchKontaktregisterReportData() {
        final ZonedDateTime startTime = ZonedDateTime.now();
        logger.info(format("Starting fetch from kontaktregister reports %s ", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        try {
            dataTransfer.transfer(lastDatapoint.get(kontaktregister.seriesId()), startTime);
        } catch (MapperError e) {
            logger.info("No data from KRR, nothing to do", e);
        }

        final ZonedDateTime endTime = ZonedDateTime.now();
        logger.info(format("Finish data transfer to %s at %s", kontaktregister.getIdWithBracket(), endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        logger.info(format("It took me %d milliseconds", ((endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli()))));
    }
}
