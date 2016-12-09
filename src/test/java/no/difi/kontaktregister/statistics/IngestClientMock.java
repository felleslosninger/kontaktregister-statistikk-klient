package no.difi.kontaktregister.statistics;

import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.exception.DataPointAlreadyExists;
import no.difi.statistics.ingest.client.exception.MalformedUrl;
import no.difi.statistics.ingest.client.exception.Unauthorized;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

public class IngestClientMock extends IngestClient {
    public static final String unauthorized = "unauthorized";
    public static final String existing_datapoint = "existingDatapoint";

    public String seriesName;
    public TimeSeriesPoint timeSeriesPoint;
    public Distance distance;

    public IngestClientMock(String baseURL, int connectionTimeout, int readTimeout, String owner, String username, String password) throws MalformedUrl {
        super(baseURL, connectionTimeout, readTimeout, owner, username, password);
    }

    @Override
    public void ingest(String seriesName, Distance distance, TimeSeriesPoint timeSeriesPoint) {
        if (seriesName.equalsIgnoreCase(unauthorized)) {
            throw new Unauthorized("unauthorized");
        }
        else if (seriesName.equalsIgnoreCase(existing_datapoint)) {
            throw new DataPointAlreadyExists();
        }

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
