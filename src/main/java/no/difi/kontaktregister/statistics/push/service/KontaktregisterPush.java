package no.difi.kontaktregister.statistics.push.service;

import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.maskinporten.MaskinportenIntegration;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.IngestService;
import no.difi.statistics.ingest.client.model.IngestResponse;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

import static no.difi.statistics.ingest.client.model.IngestResponse.Status.Ok;
import static no.difi.statistics.ingest.client.model.MeasurementDistance.hours;
import static no.difi.statistics.ingest.client.model.TimeSeriesDefinition.timeSeriesDefinition;

@Service
public class KontaktregisterPush {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KontaktregisterFetch fetch;
    private final StatisticsMapper mapper;
    private final IngestClient ingestClient;

    private final MaskinportenIntegration maskinportenIntegration;

    @Autowired
    public KontaktregisterPush(KontaktregisterFetch fetch, StatisticsMapper mapper, IngestClient ingestClient, MaskinportenIntegration maskinportenIntegration) {
        this.fetch = fetch;
        this.mapper = mapper;
        this.ingestClient = ingestClient;
        this.maskinportenIntegration = maskinportenIntegration;
    }



    public void perform(String seriesName, List<TimeSeriesPoint> points) {
        String accessToken = fetchAccessTokenFromMaskinporten();
        final TimeSeriesDefinition tsd = timeSeriesDefinition().name(seriesName).distance(hours);
        logger.info(
                "Pushing {} data points from {} to {}",
                points.size(),
                points.get(0).getTimestamp(),
                points.get(points.size() - 1).getTimestamp()
        );
        IngestResponse response;
        try {
            response = ingestClient.ingest(tsd, points, accessToken);
        } catch (IngestService.Unauthorized e) {
            logger.info("Unautorized access-token: " + e.getMessage()); // normal flow, do not want to log stacktrace as error.
            accessToken = maskinportenIntegration.acquireNewAccessToken();
            response = ingestClient.ingest(tsd, points, accessToken);
            logger.info("New access-token fetched from Maskinporten and used successfully against inndata-api.");
        }
        if (!response.ok()) {
            logger.warn("Following points could not be pushed:\n" + generateReport(response, points));
        }

    }

    private String fetchAccessTokenFromMaskinporten() {
        String accessToken = maskinportenIntegration.acquireAccessToken();
        if (maskinportenIntegration.getExpiresIn() != null) {
            final long now = Clock.systemUTC().millis();
            final long expiresTimeInMs = now + (maskinportenIntegration.getExpiresIn() * 1000);
            if (expiresTimeInMs <= now) {
                accessToken = maskinportenIntegration.acquireNewAccessToken();
            }
        }
        return accessToken;
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
