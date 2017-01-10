package no.difi.kontaktregister.statistics.transfer;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.service.KontaktregisterFetch;
import no.difi.kontaktregister.statistics.push.mapper.MapperError;
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
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.difi.kontaktregister.statistics.testutils.KontaktregisterFieldObjectMother.createaValidKontaktregisterField;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@DisplayName("When transferring data from kontaktregister to statistics")
public class DataTransferTest {
    private DataTransfer dataTransfer;

    private static final ZonedDateTime from = ZonedDateTime.now().minusDays(7);
    private static final ZonedDateTime to = ZonedDateTime.now().minusDays(1);

    @Mock private KontaktregisterFetch fetchMock;
    @Mock private KontaktregisterPush pushMock;
    @Mock private StatisticsMapper mapperMock;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        dataTransfer = new DataTransfer(fetchMock, pushMock, mapperMock);
    }

    @Test
    @DisplayName("It should call fetch.perform twice")
    public void shouldRunFetchTwiceWhenShedulerRuns() {
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject(), anyObject())).thenReturn(fields);

        dataTransfer.transfer(from, to);

        verify(fetchMock, times(2)).perform(anyString(), anyObject(), anyObject());
    }

    @Test
    @DisplayName("Should attempt to fetch D5 report and D7 report")
    public void shouldAttemptToFetchDataFromD5AndD7ReportInKontaktregisterWhenSchedulerIsActivated() {
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject(), anyObject())).thenReturn(fields);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        dataTransfer.transfer(from, to);

        verify(fetchMock, times(2)).perform(captor.capture(), anyObject(), anyObject());

        assertAll(() -> assertTrue(captor.getAllValues().contains(D5.getId()), D5.getId()),
                () -> assertTrue(captor.getAllValues().contains(D7.getId()), D7.getId()));
    }

    @Test
    @DisplayName("Should abort further processing when report from kontaktregister does not contain data")
    public void shouldAbortFurtherProcessingWhenReportDoesNotContainData() {
        when(fetchMock.perform(anyString(), anyObject(), anyObject())).thenReturn(new KontaktregisterField[]{});

        assertThrows(MapperError.class, () -> dataTransfer.transfer(from, to));
    }

    @Test
    @DisplayName("Should join reports before mapping when reports are fetched from kontaktregisteret")
    public void shouldJoinReportsBeforeMappingWhenReportsAreFetchedFromKontaktregisteret() {
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject(), anyObject())).thenReturn(fields);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        dataTransfer.transfer(null, null);

        verify(mapperMock, times(1)).map(captor.capture(), anyObject());
        assertEquals(captor.getValue().size(), 2);
    }

    @Test
    @DisplayName("Should push data to statistics when report is mapped")
    public void shouldPushReportDataToStatisticsWhenReportIsMapped() {
        Measurement m0 = new Measurement("abc", 1L);
        Measurement m1 = new Measurement("def", 2L);
        Measurement m2 = new Measurement("ghi", 3L);
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject(), anyObject())).thenReturn(fields);
        when(mapperMock.map(anyObject(), anyObject())).thenReturn(createTimeSeriesPoint(m0, m1, m2));
        ArgumentCaptor<String> sCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TimeSeriesPoint> tCaptor = ArgumentCaptor.forClass(TimeSeriesPoint.class);

        dataTransfer.transfer(null, null);

        verify(pushMock, times(1)).perform(sCaptor.capture(), tCaptor.capture());
        assertAll(
                () -> assertEquals(tCaptor.getValue().getMeasurements().get(0).getValue(), m0.getValue()),
                () -> assertEquals(tCaptor.getValue().getMeasurements().get(1).getValue(), m1.getValue()),
                () -> assertEquals(tCaptor.getValue().getMeasurements().get(2).getValue(), m2.getValue()));
    }

    @Test
    @DisplayName("Should push multiple time series points to statistics when in report")
    public void shouldPushMultipleReportDataToStatisticsWhenReportIsMappedAndMoreTimeSeriesPoints() {
        Measurement m0 = new Measurement("abc", 1L);
        Measurement m1 = new Measurement("def", 2L);
        Measurement m2 = new Measurement("ghi", 3L);
        final KontaktregisterField[] fields = {createaValidKontaktregisterField()};
        when(fetchMock.perform(anyString(), anyObject(), anyObject())).thenReturn(fields);
        when(mapperMock.map(anyObject(), anyObject())).thenReturn(createTimeSeriesPointList(m0, m1, m2));
        ArgumentCaptor<String> sCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> tCaptor = ArgumentCaptor.forClass(List.class);

        dataTransfer.transfer(null, null);

        verify(pushMock, times(1)).perform(sCaptor.capture(), tCaptor.capture());
        assertAll(
                () -> assertTrue(((TimeSeriesPoint) tCaptor.getValue().get(0)).getMeasurements().get(0).equals(m0)),
                () -> assertTrue(((TimeSeriesPoint) tCaptor.getValue().get(1)).getMeasurements().get(0).equals(m1)),
                () -> assertTrue(((TimeSeriesPoint) tCaptor.getValue().get(2)).getMeasurements().get(0).equals(m2))
        );
    }

    private List<TimeSeriesPoint> createTimeSeriesPointList(Measurement... measurement) {
        List<TimeSeriesPoint> tsp = new ArrayList<>();
        for (Measurement m : measurement) {
            tsp.add(TimeSeriesPoint.builder()
                    .timestamp(ZonedDateTime.now())
                    .measurements(singletonList(m))
                    .build());
        }
        return tsp;
    }

    private List<TimeSeriesPoint> createTimeSeriesPoint(Measurement... measurement) {
        List<TimeSeriesPoint> tsp = new ArrayList<>();
        tsp.add(TimeSeriesPoint.builder()
                .timestamp(ZonedDateTime.now())
                .measurements(asList(measurement))
                .build());
        return tsp;
    }
}