package no.difi.kontaktregister.statistics.schedule;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.StatisticsMapper;
import no.difi.kontaktregister.statistics.push.service.KontaktregisterPush;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static no.difi.kontaktregister.statistics.testutils.KontaktregisterFieldObjectMother.createaValidKontaktregisterField;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static no.difi.kontaktregister.statistics.util.StatisticsReportType.kontaktregister;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumingThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@DisplayName("When scheduler of data retrieval from kontaktregisteret is activated")
public class KontaktregisterSchedulerTest {
    private KontaktregisterScheduler scheduler;

    @Mock private KontaktregisterFetch fetchMock;
    @Mock private KontaktregisterPush pushMock;
    @Mock private StatisticsMapper mapperMock;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        scheduler = new KontaktregisterScheduler(fetchMock, pushMock, mapperMock);
    }

    @Test
    @DisplayName("It should call fetch.perform twice")
    public void shouldRunFetchTwiceWhenShedulerRuns() {
        when(fetchMock.perform(anyString(), anyObject())).thenReturn(new KontaktregisterField[]{});

        scheduler.fetchKontaktregisterReportData();

        verify(fetchMock, times(2)).perform(anyString(), anyObject());
    }

    @Test
    @DisplayName("Should attempt to fetch D5 report and D7 report")
    public void shouldAttemptToFetchDataFromD5AndD7ReportInKontaktregisterWhenSchedulerIsActivated() {
        when(fetchMock.perform(anyString(), anyObject())).thenReturn(new KontaktregisterField[]{});
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        scheduler.fetchKontaktregisterReportData();

        verify(fetchMock, times(2)).perform(captor.capture(), anyObject());

        assertAll(() -> assertTrue(captor.getAllValues().contains(D5.getId()), D5.getId()),
                  () -> assertTrue(captor.getAllValues().contains(D7.getId()), D7.getId()));
    }

    @Test
    @DisplayName("Should abort further processing when report from kontaktregister does not contain data")
    public void shouldAbortFurtherProcessingWhenReportDoesNotContainData() {
        when(fetchMock.perform(anyString(), anyObject())).thenReturn(new KontaktregisterField[]{});

        scheduler.fetchKontaktregisterReportData();

        verify(mapperMock, never()).map(anyObject(), anyObject());
        verify(pushMock, never()).perform(anyString(), anyObject());
    }

    @Test
    @DisplayName("Should join reports before mapping when reports are fetched from kontaktregisteret")
    public void shouldJoinReportsBeforeMappingWhenReportsAreFetchedFromKontaktregisteret() {
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject())).thenReturn(fields);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        scheduler.fetchKontaktregisterReportData();

        verify(mapperMock, times(1)).map(captor.capture(), anyObject());
        assertEquals(captor.getValue().size(), 2);
    }

    @Test
    @DisplayName("Should push data to statistics when report is mapped")
    public void shouldPushReportDataToStatisticsWhenReportIsMapped() {
        Measurement m0 = new Measurement("abc", 1L);
        Measurement m1 = new Measurement("abc", 1L);
        Measurement m2 = new Measurement("abc", 1L);
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject())).thenReturn(fields);
        when(mapperMock.map(anyObject(), anyObject())).thenReturn(createTimeSeriesPoint(m0, m1, m2));
        ArgumentCaptor<String> sCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TimeSeriesPoint> tCaptor = ArgumentCaptor.forClass(TimeSeriesPoint.class);

        scheduler.fetchKontaktregisterReportData();

        verify(pushMock, times(1)).perform(sCaptor.capture(), tCaptor.capture());
        assumingThat(sCaptor.getValue().equals(kontaktregister.getStatisticId()),
                () -> assertEquals(tCaptor.getValue().getMeasurements().size(), 3));
        assertAll(
                () -> assertTrue(tCaptor.getValue().getMeasurements().get(0).equals(m0)),
                () -> assertTrue(tCaptor.getValue().getMeasurements().get(1).equals(m1)),
                () -> assertTrue(tCaptor.getValue().getMeasurements().get(2).equals(m2))
        );
    }

    private TimeSeriesPoint createTimeSeriesPoint(Measurement... measurement) {
        return TimeSeriesPoint.builder()
                .timestamp(ZonedDateTime.now())
                .measurements(Arrays.asList(measurement))
                .build();
    }
}