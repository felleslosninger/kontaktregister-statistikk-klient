package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterFields;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("When mapping D5 report from Kontaktregister to Statistics")
public class StatisticsMapperD5Test {
    private StatisticsMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new StatisticsMapper();
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when no fields is defined")
    public void shouldFailMappingWhenMeasurementIsNotGiven() {
        assertThrows(MapperError.class, () -> mapper.mapD5(null, now()));
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when enum value for report is not found")
    public void shouldFailMappingWhenEnumValueDoesNotExist() {
        assertThrows(MapperError.class, () -> mapper.mapD5(singletonList(createKontaktregisterFields("notValidValue", "99")), now()));
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when value element is not a number")
    public void shouldFailMappingWhenValueIsNotANumber() {
        assertThrows(MapperError.class, () -> mapper.mapD5(singletonList(createKontaktregisterFields("Toalt antall brukere i kontaktregisteret", "99n8")), now()));
    }

    @Test
    @DisplayName("Mapping should succeed when valid datapoint")
    public void shouldSucceedWhenValuesForDatapointIsValid() {
        final TimeSeriesPoint result = mapper.mapD5(singletonList(createaValidKontaktregisterField()), now());

        assertAll(
                () -> assertEquals(ReportD5.D5_9.name(), result.getMeasurements().get(0).getId()),
                () -> assertEquals(88L, result.getMeasurements().get(0).getValue())
        );
    }

    @Test
    @DisplayName("Mapping should succeed with multiple datapoints")
    public void shouldSucceedWhenThreeDatapointsIsGiven() {
        final TimeSeriesPoint result = mapper.mapD5(asList(
                createaValidKontaktregisterField(),
                createKontaktregisterFields("Aktive brukere med reservasjon uten verken e-post eller mobil", "72"),
                createKontaktregisterFields("Aktive brukere med mobil", "91")), now());

        assertAll(
                () -> assertEquals(ReportD5.D5_9.name(), result.getMeasurements().get(0).getId()),
                () -> assertEquals(88L, result.getMeasurements().get(0).getValue()),
                () -> assertEquals(ReportD5.D5_6.name(), result.getMeasurements().get(1).getId()),
                () -> assertEquals(72L, result.getMeasurements().get(1).getValue()),
                () -> assertEquals(ReportD5.D5_2.name(), result.getMeasurements().get(2).getId()),
                () -> assertEquals(91L, result.getMeasurements().get(2).getValue())
        );
    }

    private static KontaktregisterFields createaValidKontaktregisterField() {
        return createKontaktregisterFields("Toalt antall brukere i kontaktregisteret", "88");
    }

    private static KontaktregisterFields createKontaktregisterFields(String... values) {
        if ((values.length % 2) != 0) {
            throw new IllegalArgumentException("Must be two values for each object");
        }

        KontaktregisterFields fields = new KontaktregisterFields();
        for (String value : values) {
            fields.getValues().add(createKontaktregisterValue(value));
        }
        return fields;
    }

    private static KontaktregisterValue createKontaktregisterValue(String value) {
        KontaktregisterValue krv = new KontaktregisterValue();
        krv.setValue(value);
        return krv;
    }
}