package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

public class KontaktregisterPush {
    private final IngestClient ingestClient;

    public KontaktregisterPush(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public void perform(String seriesName, TimeSeriesPoint timeSeriesPoint) {
        ingestClient.ingest(seriesName, Distance.hour, timeSeriesPoint);
    }
}
