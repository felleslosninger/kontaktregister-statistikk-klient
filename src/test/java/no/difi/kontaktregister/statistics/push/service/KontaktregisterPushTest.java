package no.difi.kontaktregister.statistics.push.service;

import no.difi.kontaktregister.statistics.IngestClientMock;
import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class KontaktregisterPushTest {
    private static final String OWNER = "owner";
    private KontaktregisterPush pushService;
    private IngestClientMock ingestClientMock;

    @BeforeEach
    public void setUp() {
        ingestClientMock = new IngestClientMock("baseurl", OWNER, "username", "password");
        pushService = new KontaktregisterPush(ingestClientMock);
    }

    @Nested
    @DisplayName("When pushing data")
    class PushDataToStatistics {
        @Test
        @DisplayName("Minutes in statistics API should not be called")
        public void shouldNeverCallMinutesInIngestClient() {
            pushService.perform(OWNER, createTimeSeries());

            assertFalse(ingestClientMock.distance == Distance.minute);
        }

        @Test
        @DisplayName("Hours in statistics API should be called")
        public void shouldCallHoursInIngestClient() {
            pushService.perform(OWNER, createTimeSeries());

            assertTrue(ingestClientMock.distance == Distance.hour);
        }

        @Test
        @DisplayName("Hours API should have valid parameters")
        public void shouldCallHoursWithValidParameters() {
            final TimeSeriesPoint timeSeries = createTimeSeries();
            pushService.perform(OWNER, timeSeries);

            assertAll(() -> assertEquals(ingestClientMock.distance, Distance.hour),
                    () -> assertEquals(ingestClientMock.seriesName, OWNER),
                    () -> assertEquals(ingestClientMock.timeSeriesPoint, timeSeries));
        }

        private TimeSeriesPoint createTimeSeries() {
            return TimeSeriesPoint.builder().timestamp(ZonedDateTime.now()).build();
        }
    }
}
