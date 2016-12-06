package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.exception.CommunicationError;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class KontaktregisterPush {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IngestClient ingestClient;

    public KontaktregisterPush(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public void perform(String seriesName, TimeSeriesPoint timeSeriesPoint) {
        try {
            ingestClient.ingest(seriesName, Distance.hour, timeSeriesPoint);
        } catch (CommunicationError ce) {
            logger.info(format("Failed ingest. Series name: %s with %d points", seriesName, timeSeriesPoint.getMeasurements().size()));
            for (Measurement measurement : timeSeriesPoint.getMeasurements()) {
                logger.info(format("  id: %s value: %d", measurement.getId(), measurement.getValue()));
            }
        }
    }
}
