package no.difi.kontaktregister.statistics.push.service;

import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.IngestResponse;
import no.difi.statistics.ingest.client.model.MeasurementDistance;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static no.difi.statistics.ingest.client.model.TimeSeriesPoint.timeSeriesPoint;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class KontaktregisterPushTest {

    private static final String OWNER = "991825827";

    @Mock private IngestClient ingestClientMock;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        when(ingestClientMock.ingest(any(TimeSeriesDefinition.class), any())).thenReturn(IngestResponse.builder().build());
    }

    @Nested
    @DisplayName("When pushing data")
    class PushDataToStatistics {
        private KontaktregisterPush pushService;

        @BeforeEach
        public void setUp() {
            pushService = new KontaktregisterPush(ingestClientMock);
        }

        @Test
        @DisplayName("Should use bulk API when more than one TimeSeriesPoint")
        public void shouldUseBulkAPIWhenMoreThanOneTimeSeriesPoint() {
            ArgumentCaptor<TimeSeriesDefinition> tsdCaptor = ArgumentCaptor.forClass(TimeSeriesDefinition.class);
            ArgumentCaptor<List> tspCaptor = ArgumentCaptor.forClass(List.class);
            List<TimeSeriesPoint> timeSeries = asList(createTimeSeries(1), createTimeSeries(2));

            pushService.perform(OWNER, timeSeries);

            assertAll(
                    () -> verify(ingestClientMock, times(1)).ingest(tsdCaptor.capture(), tspCaptor.capture()),
                    () -> assertEquals(tsdCaptor.getValue().getName(), "991825827"),
                    () -> assertEquals(tsdCaptor.getValue().getDistance(), MeasurementDistance.hours),
                    () -> assertEquals(tspCaptor.getValue().size(), 2)
            );
        }

        private TimeSeriesPoint createTimeSeries(int plusHour) {
            return timeSeriesPoint().timestamp(ZonedDateTime.now().plusHours(plusHour)).measurement("a", 2L).build();
        }
    }

}
