package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.IngestResponse;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static no.difi.statistics.ingest.client.model.IngestResponse.Status.Ok;
import static no.difi.statistics.ingest.client.model.MeasurementDistance.hours;
import static no.difi.statistics.ingest.client.model.TimeSeriesDefinition.timeSeriesDefinition;

@Component
public class KontaktregisterPush {

    private final IngestClient ingestClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public KontaktregisterPush(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    public void perform(String seriesName, List<TimeSeriesPoint> points) {
        final TimeSeriesDefinition tsd = timeSeriesDefinition().name(seriesName).distance(hours);
        logger.info(
                "Pushing {} data points from {} to {}",
                points.size(),
                points.get(0).getTimestamp(),
                points.get(points.size() - 1).getTimestamp()
        );
        IngestResponse response = ingestClient.ingest(tsd, points);
        if (!response.ok()) {
            logger.warn("Following points could not be pushed:\n" + generateReport(response, points));
        }

    }

    private String generateReport(IngestResponse response, List<TimeSeriesPoint> points) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < response.getStatuses().size(); i++) {
            if (response.getStatuses().get(i) != Ok)
                buf.append(response.getStatuses().get(i)).append(": ").append(points.get(i)).append("\n");
        }
        return buf.toString();
    }
}
