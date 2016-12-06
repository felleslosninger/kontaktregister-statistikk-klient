package no.difi.kontaktregister.statistics.schedule;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterFields;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.kontaktregister.statistics.util.ReportType;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.difi.kontaktregister.statistics.util.ReportType.D5;
import static no.difi.kontaktregister.statistics.util.ReportType.D7;

public class KontaktregisterScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String CRON_FIVE_MINUTE_INTERVAL = "0 */1 * * * *";
    private static final String CRON_FIVE_MINUTES_PAST_HOUR = "0 5 * * * *";
    private static final String CRON_SIX_MINUTES_PAST_HOUR = "0 6 * * * *";

    private final KontaktregisterFetch kontaktregisterFetch;
    private final KontaktregisterPush kontaktregisterPush;
    private final StatisticsMapper statisticsMapper;

    public KontaktregisterScheduler(KontaktregisterFetch kontaktregisterFetch, KontaktregisterPush kontaktregisterPush, StatisticsMapper statisticsMapper) {
        this.kontaktregisterFetch = kontaktregisterFetch;
        this.kontaktregisterPush = kontaktregisterPush;
        this.statisticsMapper = statisticsMapper;
    }

    /***
     * Rapport D7: Øyeblikksstatus for Digital Postkasse
     */
    @Scheduled(cron = CRON_FIVE_MINUTE_INTERVAL)
    public void fetchKontaktregisterD7Report() {
        //TODO: Retrieve last inserted datapoint from statistics.
        final ZonedDateTime startTime = ZonedDateTime.now();
        logger.info(format("%s Starting fetch at %s", D7.getNameWithBracket(), startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        ZonedDateTime reportTime = startTime.minusHours(1);
        final List<KontaktregisterFields> fields = asList(kontaktregisterFetch.perform(D7.getId(), reportTime));

        logger.info(format("%s Starting insert at %s", D7.getNameWithBracket(), ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        if (fields.size() == 0 || fields.get(0).getValues().size() == 0) {
            logger.info("No data in KRR, nothing to do");
        }
        else {
            final TimeSeriesPoint point = statisticsMapper.mapD7(fields, startTime);
            kontaktregisterPush.perform(D7.getSerieId(), point);
        }

        final ZonedDateTime endTime = ZonedDateTime.now();
        logger.info(format("%s Finish data transfer at %s", D7.getNameWithBracket(), endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        logger.info(format("It took me %d seconds", ((endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli()) / 1000)));
    }

    /***
     * Rapport D5: Øyeblikksstatus for kontakt- og reservasjonsregisteret
     */
    @Scheduled(cron = CRON_FIVE_MINUTE_INTERVAL)
    public void fetchKontaktregisterD5Report() {
        //TODO: Retrieve last inserted datapoint from statistics.
        final ZonedDateTime startTime = ZonedDateTime.now();
        logger.info(format("%s Starting fetch at %s", D5.getNameWithBracket(), startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        ZonedDateTime reportTime = startTime.minusHours(1);
        final List<KontaktregisterFields> fields = asList(kontaktregisterFetch.perform(D5.getId(), reportTime));

        logger.info(format("%s Starting insert at %s", D5.getNameWithBracket(), ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        if (fields.size() == 0 || fields.get(0).getValues().size() == 0) {
            logger.info("No data in KRR, nothing to do");
        }
        else {
            final TimeSeriesPoint point = statisticsMapper.mapD5(fields, startTime);
            kontaktregisterPush.perform(D5.getSerieId(), point);
        }

        final ZonedDateTime endTime = ZonedDateTime.now();
        logger.info(format("%s Finish data transfer at %s", D5.getNameWithBracket(), endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        logger.info(format("It took me %d seconds", ((endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli()) / 1000)));
    }
}
