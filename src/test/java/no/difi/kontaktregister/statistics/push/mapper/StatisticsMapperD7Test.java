package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterFields;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When mapping D7 report from Kontaktregister to Statistics")
public class StatisticsMapperD7Test {
    private StatisticsMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new StatisticsMapper();
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when no fields is defined")
    public void shouldFailMappingWhenMeasurementIsNotGiven() {
        assertThrows(MapperError.class, () -> mapper.mapD7(null, now()));
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when mapping to id is not to be found")
    public void shouldFailMappingWhenIdIsNotToBeFoundInEnum() {
        assertThrows(MapperError.class, () -> mapper.mapD7(singletonList(createKontaktregisterField("Inaktiv", "eBoks", "996460320", "15")), now()));
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when value element is not a number")
    public void shouldFailMappingWhenValueElementIsNotANumber() {
        assertThrows(MapperError.class, () -> mapper.mapD7(singletonList(createKontaktregisterField("Inaktive postbokser", "eBoks", "996460320", "1a5")), now()));
    }

    @Test
    @DisplayName("Mapping should consider summary as last row to map")
    public void shouldConsiderSummaryAsLastRow() {
        final TimeSeriesPoint mapped = mapper.mapD7(singletonList(createKontaktregisterField("Sum:", "", "", "2345")), now());
        final Measurement result = mapped.getMeasurements().get(0);

        assertAll(
                () -> assertEquals(ReportD7.D7_7.id(), result.getId()),
                () -> assertEquals(2345L, result.getValue())
        );
    }

    @Test
    @DisplayName("Should map to point when valid value is given")
    public void shouldMapPointWithTimeWhenValidValueAndId() {
        final TimeSeriesPoint tsp = mapper.mapD7(singletonList(createValidKontaktregisterField()), now());
        final Measurement result = tsp.getMeasurements().get(0);

        assertAll(
                () -> assertEquals(ReportD7.D7_3.id(), result.getId()),
                () -> assertEquals(15L, result.getValue())
        );
    }

    @Test
    @DisplayName("Should map multiple points when valid value is given")
    public void shouldMapPointWithTimeWhenValidValueAndIdForMultipleElements() {
        final TimeSeriesPoint tsp = mapper.mapD7(asList(
                        createValidKontaktregisterField(),
                        createKontaktregisterField("Aktive postbokser", "Digipost", "984661185", "88")), now());
        final List<Measurement> result = tsp.getMeasurements();

        assertAll(
                () -> assertEquals(ReportD7.D7_3.id(), result.get(0).getId()),
                () -> assertEquals(15L, result.get(0).getValue()),
                () -> assertEquals(ReportD7.D7_5.id(), result.get(1).getId()),
                () -> assertEquals(88L, result.get(1).getValue())
        );
    }

    @Test
    @DisplayName("Should map multiple points and handle that sum has not same amount of lines")
    public void shouldMapMultiplePointsAndHandleSumWhenMappingMultipleElements() {
        final TimeSeriesPoint tsp = mapper.mapD7(asList(
                        createValidKontaktregisterField(),
                        createKontaktregisterField("Aktive postbokser", "Digipost", "984661185", "88"),
                        createKontaktregisterField("Sum:", "", "", "982")
                ), now());
        final List<Measurement> result = tsp.getMeasurements();

        assertAll(
                () -> assertEquals(ReportD7.D7_3.id(), result.get(0).getId()),
                () -> assertEquals(15L, result.get(0).getValue()),
                () -> assertEquals(ReportD7.D7_5.id(), result.get(1).getId()),
                () -> assertEquals(88L, result.get(1).getValue()),
                () -> assertEquals(ReportD7.D7_7.id(), result.get(2).getId()),
                () -> assertEquals(982, result.get(2).getValue())
        );
    }

    @Test
    @DisplayName("Should handle that sum is in between other points")
    public void shouldHandleThatSumIsInBetweenOtherPointsWhenMappingMultipleElements() {
        final TimeSeriesPoint tsp = mapper.mapD7(asList(
                        createValidKontaktregisterField(),
                        createKontaktregisterField("Sum:", "", "", "982"),
                        createKontaktregisterField("Aktive postbokser", "Digipost", "984661185", "88")
                ), now());
        final List<Measurement> result = tsp.getMeasurements();

        assertAll(
                () -> assertEquals(ReportD7.D7_3.id(), result.get(0).getId()),
                () -> assertEquals(15L, result.get(0).getValue()),
                () -> assertEquals(ReportD7.D7_7.id(), result.get(1).getId()),
                () -> assertEquals(982, result.get(1).getValue()),
                () -> assertEquals(ReportD7.D7_5.id(), result.get(2).getId()),
                () -> assertEquals(88L, result.get(2).getValue())
        );
    }

    private static KontaktregisterFields createValidKontaktregisterField() {
        return createKontaktregisterField("Inaktive postbokser", "eBoks", "996460320", "15");
    }

    private static KontaktregisterFields createKontaktregisterField(String... values) {
        KontaktregisterFields field = new KontaktregisterFields();
        for (String value : values) {
            field.getValues().add(createKontaktregisterValue(value));
        }
        return field;
    }

    private static KontaktregisterValue createKontaktregisterValue(String value) {
        KontaktregisterValue krv = new KontaktregisterValue();
        krv.setValue(value);
        return krv;
    }
}