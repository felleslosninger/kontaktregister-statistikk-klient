package no.difi.kontaktregister.statistics.push.service;

import ch.qos.logback.core.Appender;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.MeasurementDistance;
import no.difi.statistics.ingest.client.model.TimeSeriesDefinition;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class KontaktregisterPushTest {

    private ch.qos.logback.classic.Logger root;

    private static final String OWNER = "991825827";

    @Mock private Appender<ch.qos.logback.classic.spi.ILoggingEvent> appenderMock;
    @Mock private IngestClient ingestClientMock;

    @BeforeEach
    public void setUp() throws Exception {
        initMocks(this);

        when(appenderMock.getName()).thenReturn("MOCK");
        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.addAppender(appenderMock);
    }

    @AfterEach
    public void tearDown() {
        root.detachAppender(appenderMock);
    }

    @Test
    public void test() {
        assertTrue(true);
    }

    @Nested
    @DisplayName("When pushing data")
    class PushDataToStatistics {
        private KontaktregisterPush pushService;

        @BeforeEach
        public void setUp() throws MalformedURLException {
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
            return TimeSeriesPoint.builder().timestamp(ZonedDateTime.now().plusHours(plusHour)).build();
        }
    }

}
