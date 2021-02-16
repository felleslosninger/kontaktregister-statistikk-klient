package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.Properties;
import no.difi.statistics.ingest.client.IngestClient;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.time.ZonedDateTime.now;
import static no.difi.statistics.ingest.client.model.TimeSeriesPoint.timeSeriesPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@DisplayName("When retrieving last datapoint for kontaktregister from statistics")
public class LastDatapointTest {
    private static final String seriesId = "kontaktogreservasjonsregister";
    private LastDatapoint lastDatapoint;

    @Mock
    private IngestClient ingestClientMock;

    @Mock
    private Properties properties;

    private final ZonedDateTime baseTime = now().minusYears(3).truncatedTo(ChronoUnit.HOURS);

    @BeforeEach
    public void setUp() {
        initMocks(this);

        lastDatapoint = new LastDatapoint(ingestClientMock, properties);
        when(properties.getBaseLine()).thenReturn(baseTime);
    }

    @Test
    @DisplayName("It should return ZoneDateTime set to 15.05.01T00 when no datapoint is found")
    public void shouldReturnNullWhenResponseCode200AndEmptyDataset() {
        when(ingestClientMock.last(anyObject())).thenReturn(Optional.empty());
        assertEquals(baseTime, lastDatapoint.get(seriesId));
    }

    @Test
    @DisplayName("It should return ZoneDateTime type when response containts value")
    public void shouldReturnZoneDateTimeWithValueFromResponseWhenResponseCode200AndValueExistsInDataset() {
        when(ingestClientMock.last(anyObject())).thenReturn(createResponseOk());
        assertTrue(lastDatapoint.get(seriesId).getClass().equals(ZonedDateTime.class));
    }

    @Test
    @DisplayName("It should return same ZoneDateTime as the timestamp in response when response contains value")
    public void shouldReturnZoneDateTimeValueFoundInResponseWhenResponseContainsValue() {
        final ZonedDateTime dateTime = ZonedDateTime.of(2016, 12, 24, 18, 0, 0, 0, ZonedDateTime.now().getZone());
        when(ingestClientMock.last(anyObject())).thenReturn(createResponseOk(dateTime));
        assertEquals(dateTime, lastDatapoint.get(seriesId));
    }

    private Optional<TimeSeriesPoint> createResponseOk() {
        return createResponseOk(now().minusDays(3));
    }


    private Optional<TimeSeriesPoint> createResponseOk(ZonedDateTime dateTime) {
        return Optional.of(timeSeriesPoint()
                .timestamp(dateTime)
                .measurement("d5_8", 31)
                .measurement("d5_9", 17)
                .measurement("d5_6", 18)
                .measurement("d5_7", 26)
                .measurement("d5_10", 57)
                .measurement("d5_4", 16)
                .measurement("Pål sine høner", 83)
                .measurement("d5_5", 62)
                .measurement("d5_2", 2)
                .measurement("d5_3", 2)
                .build());
    }

}