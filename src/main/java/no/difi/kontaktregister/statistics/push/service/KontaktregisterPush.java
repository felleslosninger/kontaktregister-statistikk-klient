package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.exception.DataPointAlreadyExists;
import no.difi.statistics.ingest.client.exception.IngestFailed;
import no.difi.statistics.ingest.client.exception.Unauthorized;
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

    public boolean perform(String seriesName, TimeSeriesPoint timeSeriesPoint) {
        try {
            ingestClient.ingest(seriesName, Distance.hour, timeSeriesPoint);
        } catch (DataPointAlreadyExists e) {
            logger.error("Whops, seems like that datapoint already exists");
            logger.error(format("Series name: %s", seriesName));
            for (Measurement measurement : timeSeriesPoint.getMeasurements()) {
                logger.error(format(" - id: %s value: %d", measurement.getId(), measurement.getValue()));
            }
        } catch (Unauthorized e) {
            logger.error("Unauthorized, time for you to check the password you gave me", e);
        } catch (IngestFailed e) {
            logger.error("Something failed when I tried to push data (my \"M$\" message)", e);
        }
        return true;
    }
}
