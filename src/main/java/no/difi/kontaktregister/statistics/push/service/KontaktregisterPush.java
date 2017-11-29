package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static no.difi.statistics.ingest.client.model.MeasurementDistance.hours;

public class KontaktregisterPush {

    private final IngestClient ingestClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public KontaktregisterPush(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public void perform(String seriesName, List<TimeSeriesPoint> timeSeriePoints) {
        final TimeSeriesDefinition tsd = TimeSeriesDefinition.builder()
                .name(seriesName)
                .distance(hours);
        logger.info("Pushing {} data points", timeSeriePoints.size());
        ingestClient.ingest(tsd, timeSeriePoints);
    }
}
