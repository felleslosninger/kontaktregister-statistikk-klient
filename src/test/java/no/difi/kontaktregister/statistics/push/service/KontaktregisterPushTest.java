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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class KontaktregisterPushTest {

    private ch.qos.logback.classic.Logger root;

    private static final String OWNER = "991825827";
    private static final String USERNAME = "991825827";
    private static final String BASEURL = "http://eid-test-docker.dmz.local";
    private static final String PASSWORD = "password";

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
        @DisplayName("Should call MeasurementDistance.hours in statistics API")
        public void shouldCallMeasurementDistanceHoursInStatistics() {
            ArgumentCaptor<TimeSeriesDefinition> tsdCaptor = ArgumentCaptor.forClass(TimeSeriesDefinition.class);

            pushService.perform(OWNER, createTimeSeries());

            assertAll(
                    () -> verify(ingestClientMock, times(1)).ingest(tsdCaptor.capture(), any(TimeSeriesPoint.class)),
                    () -> assertEquals(tsdCaptor.getValue().getName(), "991825827"),
                    () -> assertEquals(tsdCaptor.getValue().getDistance(), MeasurementDistance.hours)
            );
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

        private TimeSeriesPoint createTimeSeries() {
            return TimeSeriesPoint.builder().timestamp(ZonedDateTime.now()).build();
        }

        private TimeSeriesPoint createTimeSeries(int plusHour) {
            return TimeSeriesPoint.builder().timestamp(ZonedDateTime.now().plusHours(plusHour)).build();
        }
    }

}
