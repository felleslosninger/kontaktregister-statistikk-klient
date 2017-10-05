package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.IngestService;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.format;
import static no.difi.statistics.ingest.client.model.MeasurementDistance.hours;

public class KontaktregisterPush {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IngestClient ingestClient;

    public KontaktregisterPush(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public void perform(String seriesName, TimeSeriesPoint timeSeriesPoint) {
        try {
            ingestClient.ingest(TimeSeriesDefinition.builder().name(seriesName).distance(hours), timeSeriesPoint);
        } catch (IngestService.DataPointAlreadyExists e) {
            logger.error("Whops, seems like that datapoint already exists");
            logger.error(format("Series name: %s", seriesName));
            for (Measurement measurement : timeSeriesPoint.getMeasurements()) {
                logger.error(format(" - id: %s value: %d", measurement.getId(), measurement.getValue()));
            }
            throw e;
        }
    }

    public void perform(String seriesName, List<TimeSeriesPoint> timeSeriePoints) {
        final TimeSeriesDefinition tsd = TimeSeriesDefinition.builder()
                .name(seriesName)
                .distance(hours);
        ingestClient.ingest(tsd, timeSeriePoints);
    }
}
