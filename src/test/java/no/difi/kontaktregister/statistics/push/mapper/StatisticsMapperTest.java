package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static no.difi.kontaktregister.statistics.testutils.KontaktregisterFieldObjectMother.createKontaktregisterField;
import static no.difi.kontaktregister.statistics.util.NameTranslateDefinitions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When mapping from D5 and D7 report Kontaktregister to Statistics")
public class StatisticsMapperTest {
    private StatisticsMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new StatisticsMapper();
    }

    @Test
    @DisplayName("Mapping should fail with MapperError when no fields is defined")
    public void shouldFailMappingWhenMeasurementIsNotGiven() {
        assertThrows(MapperError.class, () -> mapper.map(null, now()));
    }

    @Test
    @DisplayName("Mapper must contain all fields")
    public void shouldFailWhenReportsDoesNotContainAllFields() {
        MapperError exception = assertThrows(MapperError.class,
                () -> mapper.map(singletonList(createKontaktregisterField(D5_5.getKrrField(), "42")), now()));
        assertTrue(exception.getMessage().matches("Measurement .* is missing.*"));
    }

    @Test
    @DisplayName("Mapper should not fail when all elements are filtered away from result")
    public void shouldNotFailWhenResultsFromMapIsEmpty() {
        MapperError exception = assertThrows(MapperError.class,
                () -> mapper.map(singletonList(createKontaktregisterField("None existing", "42")), now()));

        assertEquals(exception.getMessage(), "No valid data after index mapping");
    }

    @Test
    @DisplayName("Mapper should contain all desired fields with values in output")
    public void shouldMapFieldsWhenAvailable() {
        final List<Measurement> result = mapper.map(createValidKontaktregisterFieldListWithAllElements(), now()).get(0).getMeasurements();

        assertAll(
                () -> assertEquals(1L, result.stream().filter(e -> e.getId().equals(D5_1.getStatisticId())).findFirst().get().getValue()),
                () -> assertEquals(2L, result.stream().filter(e -> e.getId().equals(D5_2.getStatisticId())).findFirst().get().getValue()),
                () -> assertEquals(12L, result.stream().filter(e -> e.getId().equals(D5_5_6.getStatisticId())).findFirst().get().getValue()),
                () -> assertEquals(16L, result.stream().filter(e -> e.getId().equals(D5_7.getStatisticId())).findFirst().get().getValue()),
                () -> assertEquals(64L, result.stream().filter(e -> e.getId().equals(D7_3.getStatisticId())).findFirst().get().getValue()),
                () -> assertEquals(128L, result.stream().filter(e -> e.getId().equals(D7_4.getStatisticId())).findFirst().get().getValue())
        );
    }

    @Test
    @DisplayName("Mapper should not map source fields for calculation")
    public void shouldNotMapSourceFieldsForCalculationsWhenAvailable() {
        final List<Measurement> result = mapper.map(createValidKontaktregisterFieldListWithAllElements(), now()).get(0).getMeasurements();

        assertAll(
                () -> assertFalse(result.contains(find(D5_5.getKrrField()))),
                () -> assertFalse(result.contains(find(D5_6.getKrrField()))));
    }

    @Test
    @DisplayName("Mapper should contain all desired fields and discard calculation source fields and extra fields")
    public void shouldMapAllDesiredFiledsAndNotMapCalcSourcesOrUnwantedFields() {
        List<KontaktregisterField> elements = new ArrayList<>(createValidKontaktregisterFieldListWithAllElements());
        elements.add(createKontaktregisterField("Not valid field", "something", "ho ho ho", "1069"));
        elements.add(createKontaktregisterField("Winter is coming", "1072"));
        elements.add(createKontaktregisterField("Christmas", "Soon", "1009"));

        final List<Measurement> result = mapper.map(elements, now()).get(0).getMeasurements();

        assertAll(
                () -> assertEquals(empty(), result.stream().filter(e -> e.getId().equals(D5_5.getStatisticId())).findFirst()),
                () -> assertEquals(empty(), result.stream().filter(e -> e.getId().equals("ChristmasSoon")).findFirst()),
                () -> assertEquals(empty(), result.stream().filter(e -> e.getId().equals("Winter is coming")).findFirst()),
                () -> assertEquals(empty(), result.stream().filter(e -> e.getValue() == 1072L).findFirst()),
                () -> assertEquals(empty(), result.stream().filter(e -> e.getValue() == 1069L).findFirst())
        );
    }

    @Test
    @DisplayName("Mapper should map all fields and values for extended period with larger dataset")
    public void shouldMapAllValuesAndFieldsWhenMultipleValuesForEachFieldIsInResultset() {
        final List<KontaktregisterField> elements = createValidKontaktregisterFieldListWithAllElementsAndMultipleValuesForEachField();

        List<TimeSeriesPoint> tsp = mapper.map(elements, now());

        for (int index = 0; index < tsp.size(); index++) {
            assertMeasurement(tsp.get(index).getMeasurements(), index+1);
        }
    }

    @Test
    @DisplayName("Mapper should map list of datapoints with correct times")
    public void shouldMapDatapointTimeCorrectWhenMultipleTimeSeries() {
        final List<KontaktregisterField> elements = createValidKontaktregisterFieldListWithAllElementsAndMultipleValuesForEachField();

        final ZonedDateTime reportDataDateTime = now();
        List<TimeSeriesPoint> tsp = mapper.map(elements, reportDataDateTime);

        for (int index = 0; index < tsp.size(); index++) {
            assertEquals(tsp.get(index).getTimestamp().getHour(), reportDataDateTime.getHour() + index - 1);
        }
    }

    private static void assertMeasurement(List<Measurement> result, long index) {
        assertAll(
                () -> assertEquals(result.stream().filter(e -> e.getValue() == index).findFirst().get().getValue(), index),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == index*2).findFirst().get().getValue(), index*2),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == index*4 + index*8).findFirst().get().getValue(), index*4 + index*8),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == index*16).findFirst().get().getValue(), index*16),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == index*64).findFirst().get().getValue(), index*64),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == index*128).findFirst().get().getValue(), index*128)
        );
    }

    private static List<KontaktregisterField> createValidKontaktregisterFieldListWithAllElements() {
        return asList(
                createKontaktregisterField(D5_1.getKrrField(), "1"),
                createKontaktregisterField(D5_2.getKrrField(), "2"),
                createKontaktregisterField(D5_5.getKrrField(), "4"),
                createKontaktregisterField(D5_6.getKrrField(), "8"),
                createKontaktregisterField(D5_7.getKrrField(), "16"),
                createKontaktregisterField(D7_3.getKrrField(), "64"),
                createKontaktregisterField(D7_4.getKrrField(), "128")
        );
    }

    private static List<KontaktregisterField> createValidKontaktregisterFieldListWithAllElementsAndMultipleValuesForEachField() {
        return asList(
                createKontaktregisterField(D5_1.getKrrField(), "1", "2", "3", "4", "5"),
                createKontaktregisterField(D5_2.getKrrField(), "2", "4", "6", "8", "10"),
                createKontaktregisterField(D5_5.getKrrField(), "4", "8", "12", "16", "20"),
                createKontaktregisterField(D5_6.getKrrField(), "8", "16", "24", "32", "40"),
                createKontaktregisterField(D5_7.getKrrField(), "16", "32", "48", "64", "80"),
                createKontaktregisterField(D7_3.getKrrField(), "64", "128", "192", "256", "320"),
                createKontaktregisterField(D7_4.getKrrField(), "128", "256", "384", "512", "640")
        );
    }
}