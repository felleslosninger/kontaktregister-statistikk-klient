package no.difi.kontaktregister.statistics;

import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.exception.MalformedUrl;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

public class IngestClientMock extends IngestClient {
    public String seriesName;
    public TimeSeriesPoint timeSeriesPoint;
    public Distance distance;

    public IngestClientMock(String baseURL, String owner, String username, String password) throws MalformedUrl {
        super(baseURL, owner, username, password);
    }

    @Override
    public void ingest(String seriesName, Distance distance, TimeSeriesPoint timeSeriesPoint) {
        if(distance == Distance.minute) {

            this.minute(seriesName, timeSeriesPoint);
        } else if(distance == Distance.hour) {
            this.hour(seriesName, timeSeriesPoint);
        }
    }

    private void minute(String seriesName, TimeSeriesPoint timeSeriesPoint) {
        this.seriesName = seriesName;
        this.timeSeriesPoint = timeSeriesPoint;
    }

    private void hour(String seriesName, TimeSeriesPoint timeSeriesPoint) {
        this.seriesName = seriesName;
        this.timeSeriesPoint = timeSeriesPoint;
        this.distance = Distance.hour;
    }
}
