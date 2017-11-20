package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

import java.util.List;

import static no.difi.statistics.ingest.client.model.MeasurementDistance.hours;

public class KontaktregisterPush {

    private final IngestClient ingestClient;

    public KontaktregisterPush(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public void perform(String seriesName, List<TimeSeriesPoint> timeSeriePoints) {
        final TimeSeriesDefinition tsd = TimeSeriesDefinition.builder()
                .name(seriesName)
                .distance(hours);
        ingestClient.ingest(tsd, timeSeriePoints);
    }
}
