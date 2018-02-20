package no.difi.kontaktregister.statistics.transfer;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.fetch.service.LastDatapoint;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

@Component
public class DataTransfer {

    private final KontaktregisterFetch fetch;
    private final KontaktregisterPush push;
    private final StatisticsMapper mapper;
    private final LastDatapoint lastDatapoint;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public DataTransfer(KontaktregisterFetch fetch, KontaktregisterPush push, StatisticsMapper mapper, LastDatapoint lastDatapoint) {
        this.fetch = fetch;
        this.push = push;
        this.mapper = mapper;
        this.lastDatapoint = lastDatapoint;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void transfer() {
        try {
            doTransfer();
        } catch (Exception e) {
            logger.error("Transfer failed", e);
        }
    }

    private void doTransfer() {
        ZonedDateTime startTime = lastDatapoint.get(kontaktregister.seriesId()).plusHours(1);
        ZonedDateTime endTime = ZonedDateTime.now(ZoneId.of("UTC"));
        if (startTime.isAfter(endTime)) {
            logger.debug("No new data to transfer");
            return;
        }
        endTime = min(endTime, startTime.plusYears(1));
        logger.info("Transferring data from {} to {}", startTime, endTime);
        List<KontaktregisterField> fields = new ArrayList<>();
        fields.addAll(fetch.perform(D5.getId(), startTime, endTime));
        fields.addAll(fetch.perform(D7.getId(), startTime, endTime));
        removeTrailingZeroes(fields);
        List<TimeSeriesPoint> points = mapper.map(fields, startTime);
        push.perform(kontaktregister.seriesId(), points);
        logger.info("Data transfer completed");
    }

    private void removeTrailingZeroes(List<KontaktregisterField> fields) {
        while (fields.stream().map(DataTransfer::lastValue).allMatch(DataTransfer::isZero))
            fields.forEach(field -> field.getValues().remove(field.getValues().size()-1));
    }

    private static String lastValue(KontaktregisterField field) {
        return field.getValues().get(field.getValues().size()-1).getValue();
    }

    private static boolean isZero(String value) {
        try {
            return Long.parseLong(value) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static ZonedDateTime min(ZonedDateTime t1, ZonedDateTime t2) {
        return t1.isBefore(t2) ? t1 : t2;
    }

}
