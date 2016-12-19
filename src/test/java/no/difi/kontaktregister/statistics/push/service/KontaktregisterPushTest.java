package no.difi.kontaktregister.statistics.push.service;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import no.difi.kontaktregister.statistics.testutils.IngestClientMock;
import no.difi.statistics.ingest.client.Distance;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static no.difi.kontaktregister.statistics.testutils.IngestClientMock.existing_datapoint;
import static no.difi.kontaktregister.statistics.testutils.IngestClientMock.unauthorized;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class KontaktregisterPushTest {
    private KontaktregisterPush pushService;

    private ch.qos.logback.classic.Logger root;

    private static final String OWNER = "991825827";
    private static final String USERNAME = "991825827";
    private static final String BASEURL = "http://eid-test-docker.dmz.local";
    private static final String PASSWORD = "password";

    @Mock
    private Appender<ch.qos.logback.classic.spi.ILoggingEvent> appenderMock;

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

    @Nested
    @DisplayName("When pushing data")
    class PushDataToStatistics {
        private KontaktregisterPush pushService;
        private IngestClientMock ingestClientMock;

        @BeforeEach
        public void setUp() {
            ingestClientMock = new IngestClientMock("baseurl", 1000, 3000, OWNER, "username", "password");
            pushService = new KontaktregisterPush(ingestClientMock);
        }

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

    @Nested
    @DisplayName("When getting error from IngestClient")
    class ReturnValuesFromIngestClient {
        private IngestClientMock ingestClientMock;

        @BeforeEach
        public void setUp() {
            ingestClientMock = new IngestClientMock("baseurl", 1000, 3000, OWNER, "username", "password");
            pushService = new KontaktregisterPush(ingestClientMock);
        }


        @Test
        @DisplayName("Log that Authorization is missing or wrong")
        public void failRequestWhenAuthorizationIsMissing() {
            createPushService(BASEURL, OWNER, "ole", "bull").perform(unauthorized, createTimeSeriesPoint());

            verifyLogMessage(appenderMock, "Unauthorized, time for you to check the password you gave me");
        }

        @Test
        @DisplayName("Request fails with CommunicationError when measurement is not given")
        public void failRequestWithCommunicationErrorWhenMeasurementsMissingFromJson() {
            createPushService(BASEURL, OWNER, "ole", "bull").perform(existing_datapoint, createTimeSeriesPoint());

            verifyLogMessage(appenderMock, "Whops, seems like that datapoint already exists");
        }

        private KontaktregisterPush createPushService(String baseUrl, String owner, String username, String password) {
            return new KontaktregisterPush(new IngestClientMock(baseUrl, 1000, 3000, owner, username, password));
        }
    }

    @Nested
    @DisplayName("When initializing IngestClient API")
    class InitializationErrorsFromIngestClient {
        @Test
        @DisplayName("Check that log message is correct when URL is not a valid URL")
        public void shouldFailWhenNotAnUrlAsBaseUrl() {
            createPushService("feik url", OWNER, USERNAME, PASSWORD).perform("series", createTimeSeriesPoint());

            verifyLogMessage(appenderMock, "Something failed when I tried to push data (my \"M$\" message)");
        }

        @Test
        @DisplayName("Check that log message is correct when username is missing")
        public void shouldFailWhenUsernameIsMissing() {
            createPushService(BASEURL, OWNER, "", PASSWORD).perform("series", createTimeSeriesPoint());

            verifyLogMessage(appenderMock, "Something failed when I tried to push data (my \"M$\" message)");
        }

        @Test
        @DisplayName("Check that log message is correct when password is missing")
        public void shouldFailWhenPasswordIsMissing() {
            createPushService(BASEURL, OWNER, USERNAME, "").perform("series", createTimeSeriesPoint());

            verifyLogMessage(appenderMock, "Something failed when I tried to push data (my \"M$\" message)");
        }

        @Test
        @DisplayName("Check that log message is correct when owner is missing")
        public void shouldFailAndLogWhenOwnerIsMissing() {
            createPushService(BASEURL, "", USERNAME, PASSWORD).perform("series", createTimeSeriesPoint());

            verifyLogMessage(appenderMock, "Something failed when I tried to push data (my \"M$\" message)");
        }

        private KontaktregisterPush createPushService(String baseUrl, String owner, String username, String password) {
            return new KontaktregisterPush(new IngestClient(baseUrl, 1000, 3000, owner, username, password));
        }
    }

    private TimeSeriesPoint createTimeSeriesPoint() {
        return TimeSeriesPoint.builder()
                .timestamp(ZonedDateTime.now())
                .measurement(new Measurement("123", 234L))
                .build();
    }

    private void verifyLogMessage(Appender mockAppender, String logText) {
        verify(mockAppender).doAppend(argThat(new ArgumentMatcher() {
            @Override
            public boolean matches(final Object argument) {
                return ((LoggingEvent)argument).getFormattedMessage().contains(logText);
            }
        }));
    }
}
