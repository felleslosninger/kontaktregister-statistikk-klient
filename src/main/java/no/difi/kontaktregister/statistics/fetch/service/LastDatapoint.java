package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static no.difi.statistics.ingest.client.model.MeasurementDistance.hours;
import static no.difi.statistics.ingest.client.model.TimeSeriesDefinition.timeSeriesDefinition;

@Component
public class LastDatapoint {
    private final IngestClient ingestClient;
    protected final static ZonedDateTime baseTime = ZonedDateTime.of(2015, 5, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

    public LastDatapoint(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public ZonedDateTime get(String seriesName) {
        return ingestClient.last(timeSeriesDefinition().name(seriesName).distance(hours))
                .map(TimeSeriesPoint::getTimestamp).orElse(baseTime);
    }
}
