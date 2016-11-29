package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.exception.CommunicationError;
import no.difi.statistics.ingest.client.exception.MalformedUrl;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Inserting data into statistics")
public class KontaktregisterPushITest {
    private KontaktregisterPush pushService;
    private IngestClient ingestClient;

    private static final String OWNER = "991825827";
    private static final String USERNAME = "991825827";
    private static final String BASEURL = "http://eid-test-docker.dmz.local";
    private static final String PASSWORD = "^_=6BTx0Z_4%aO,eUU4x";

    @Nested
    @DisplayName("When using IngestClient API")
    class ExceptionsThrownFromIngestClient {
        @Test
        @DisplayName("Expect MalformedUrl exception when invalid url is passed to IngestClient")
        public void expectMalformedURLExceptionWhenNotUrl() {
            assertThrows(MalformedUrl.class, () -> createPushService("feik url", OWNER, USERNAME, PASSWORD).perform("series", createTimeSeriesPoint()));
        }

        @Test
        @DisplayName("Expect CommunicationError exception when connection to statistics is down, e")
        public void expectCommunicationErrorWhenStatisticsIsDown() {
            assertThrows(CommunicationError.class, () -> createPushService(BASEURL, OWNER, USERNAME, PASSWORD).perform("series", createTimeSeriesPoint()));
        }

        @Test
        @DisplayName("Expect CommunicationError exception when authorization is missing")
        public void failRequestWhenAuthorizationIsMissing() {
            assertThrows(CommunicationError.class, () -> createPushService(BASEURL, OWNER, "ole", "bull").perform("series", createTimeSeriesPoint()));
        }

        @Test
        @DisplayName("Request fails with CommunicationError when measurement is not given")
        public void failRequestWithCommunicationErrorWhenMeasurementsMissingFromJson() {
            assertThrows(CommunicationError.class, () -> createPushService(BASEURL, OWNER, "ole", "bull").perform("series", createTimeSeriesPoint(null)));
        }

        @Test
        @DisplayName("Request fails with CommunicationError when measurement id is missing")
        public void failRequestWithCommunicationErrorWhenMeasurementIdLacksFromJson() {
            assertThrows(CommunicationError.class, () -> createPushService(BASEURL, OWNER, "ole", "bull")
                    .perform("series", createTimeSeriesPoint("", 123L)));
        }

        @Test
        @DisplayName("Request fails with Nullpointer when measurement value lacks from json")
        public void failRequestWithNullpointerWhenMeasurementValueLacksFromJson() {
            assertThrows(NullPointerException.class, () -> createPushService(BASEURL, OWNER, "ole", "bull")
                    .perform("series", createTimeSeriesPoint("mId", null)));
        }

        private TimeSeriesPoint createTimeSeriesPoint(Measurement measurement) {
            return TimeSeriesPoint.builder()
                    .timestamp(ZonedDateTime.now())
                    .measurement(measurement)
                    .build();
        }

        private TimeSeriesPoint createTimeSeriesPoint(String measurementId, Long measurement) {
            return TimeSeriesPoint.builder()
                    .timestamp(ZonedDateTime.now())
                    .measurement(
                            measurementId,
                            measurement
                    )
                    .build();
        }

        private TimeSeriesPoint createTimeSeriesPoint() {
            return TimeSeriesPoint.builder()
                    .timestamp(ZonedDateTime.now())
                    .measurement(new Measurement("123", 234L))
                    .build();
        }

        private KontaktregisterPush createPushService(String baseUrl, String owner, String username, String password) {
            return new KontaktregisterPush(new IngestClient(baseUrl, owner, username, password));
        }
    }
}