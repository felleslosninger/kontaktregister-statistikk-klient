package no.difi.kontaktregister.statistics.transfer;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.MapperError;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;

public class DataTransfer {
    private final KontaktregisterFetch fetch;
    private final KontaktregisterPush push;
    private final StatisticsMapper mapper;

    public DataTransfer(KontaktregisterFetch fetch, KontaktregisterPush push, StatisticsMapper mapper) {
        this.fetch = fetch;
        this.push = push;
        this.mapper = mapper;
    }

    public void transfer(ZonedDateTime from, ZonedDateTime to) {
        final List<KontaktregisterField> d5Report = asList(fetch.perform(D5.getId(), from, to));
        final List<KontaktregisterField> d7Report = asList(fetch.perform(D7.getId(), from, to));
        if (d5Report.isEmpty()) throw new MapperError("D5 report is empty");
        if (d7Report.isEmpty()) throw new MapperError("D7 report is empty");
        List<KontaktregisterField> fields = new ArrayList<>();
        fields.addAll(d5Report);
        fields.addAll(d7Report);
        final List<TimeSeriesPoint> datapoint = mapper.map(fields, from);
        if (datapoint.size() == 1) {
            push.perform(kontaktregister.seriesId(), datapoint.get(0));
        }
        else {
            push.perform(kontaktregister.seriesId(), datapoint);
        }
    }

}
