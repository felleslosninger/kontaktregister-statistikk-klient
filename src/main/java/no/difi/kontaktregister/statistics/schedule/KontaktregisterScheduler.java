package no.difi.kontaktregister.statistics.schedule;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static no.difi.kontaktregister.statistics.util.StatisticsReportType.*;

public class KontaktregisterScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String cron_one_minute_interval = "0 */1 * * * *";
    private static final String cron_ten_minutes_past_every_hour = "0 10 * * * *";

    private final KontaktregisterFetch kontaktregisterFetch;
    private final KontaktregisterPush kontaktregisterPush;
    private final StatisticsMapper statisticsMapper;

    public KontaktregisterScheduler(KontaktregisterFetch kontaktregisterFetch, KontaktregisterPush kontaktregisterPush, StatisticsMapper statisticsMapper) {
        this.kontaktregisterFetch = kontaktregisterFetch;
        this.kontaktregisterPush = kontaktregisterPush;
        this.statisticsMapper = statisticsMapper;
    }

    /***
     * Rapport D5: Øyeblikksstatus fra Kontakt- og Reservasjonsregisteret
     */
    @Scheduled(cron = cron_ten_minutes_past_every_hour)
    public void fetchKontaktregisterReportData() {
        //TODO: Retrieve last inserted datapoint from statistics.
        final ZonedDateTime startTime = ZonedDateTime.now();
        logger.info(format("Starting fetch from %s %s at %s", D5.getNameWithBracket(), D5.getNameWithBracket(), startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        ZonedDateTime reportTime = startTime.minusHours(1);
        final List<KontaktregisterField> d5Report = asList(kontaktregisterFetch.perform(D5.getId(), reportTime));
        final List<KontaktregisterField> d7Report = asList(kontaktregisterFetch.perform(D7.getId(), reportTime));

        if (!hasReportData(d5Report, d7Report)) {
            logger.info("No data from KRR, nothing to do");
        }
        else {
            List<KontaktregisterField> fields = new ArrayList<>();
            fields.addAll(d5Report);
            fields.addAll(d7Report);

            final TimeSeriesPoint datapoint = statisticsMapper.map(fields, startTime);
            kontaktregisterPush.perform(kontaktregister.getStatisticId(), datapoint);
        }

        final ZonedDateTime endTime = ZonedDateTime.now();
        logger.info(format("Finish data transfer to %s at %s", kontaktregister.getIdWithBracket(), endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        logger.info(format("It took me %d milliseconds", ((endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli()))));
    }

    private boolean hasReportData(List<KontaktregisterField> d5Report, List<KontaktregisterField> d7Report) {
        return !(d5Report == null || d5Report.size() == 0) & !(d7Report == null || d7Report.size() == 0);
    }
}
