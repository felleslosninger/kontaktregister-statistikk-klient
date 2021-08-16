package no.difi.kontaktregister.statistics.push.service;

import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.maskinporten.MaskinportenIntegration;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static no.difi.statistics.ingest.client.model.TimeSeriesPoint.timeSeriesPoint;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class KontaktregisterPushTest {

    private static final String OWNER = "991825827";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Mock
    private MaskinportenIntegration maskinportenMock;

    @Mock
    private KontaktregisterFetch fetchMock;

    @Mock
    private StatisticsMapper mapperMock;

    @Mock
    private IngestClient ingestClientMock;


    private KontaktregisterPush kontaktregisterPush;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        kontaktregisterPush = new KontaktregisterPush(fetchMock, mapperMock, ingestClientMock, maskinportenMock);
        when(ingestClientMock.ingest(any(TimeSeriesDefinition.class), any(), any())).thenReturn(IngestResponse.builder().build());
    }

    @Nested
    @DisplayName("When pushing data")
    class PushDataToStatistics {


        @Test
        @DisplayName("Should use bulk API when more than one TimeSeriesPoint")
        public void shouldUseBulkAPIWhenMoreThanOneTimeSeriesPoint() {
            ArgumentCaptor<TimeSeriesDefinition> tsdCaptor = ArgumentCaptor.forClass(TimeSeriesDefinition.class);
            ArgumentCaptor<List> tspCaptor = ArgumentCaptor.forClass(List.class);
            List<TimeSeriesPoint> timeSeries = asList(createTimeSeries(1), createTimeSeries(2));

            when(maskinportenMock.acquireAccessToken()).thenReturn("faketoken");
            when(maskinportenMock.acquireNewAccessToken()).thenReturn("newFaketoken");

            kontaktregisterPush.perform(OWNER, timeSeries);


            assertAll(
                    () -> verify(ingestClientMock, times(1)).ingest(tsdCaptor.capture(), tspCaptor.capture(), anyString()),
                    () -> assertEquals(tsdCaptor.getValue().getName(), OWNER),
                    () -> assertEquals(tsdCaptor.getValue().getDistance(), MeasurementDistance.hours),
                    () -> assertEquals(tspCaptor.getValue().size(), 2)
            );
        }

        private TimeSeriesPoint createTimeSeries(int plusHour) {
            return timeSeriesPoint().timestamp(ZonedDateTime.now().plusHours(plusHour)).measurement("a", 2L).build();
        }
    }

}
