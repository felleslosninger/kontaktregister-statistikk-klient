package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.difi.kontaktregister.statistics.util.NameTranslateDefinitions.*;
import static no.difi.kontaktregister.statistics.testutils.KontaktregisterFieldObjectMother.createKontaktregisterField;
import static no.difi.kontaktregister.statistics.testutils.KontaktregisterFieldObjectMother.createaValidKontaktregisterField;
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
    @DisplayName("Mapper should skip field without error when not found in report definition")
    public void shouldNotFailWhenFieldIsNotFoundInEnum() {
        final TimeSeriesPoint result = mapper.map(asList(createaValidKontaktregisterField(), createKontaktregisterField("None existing", "42")), now());

        assertEquals(result.getMeasurements().size(), 1);
    }

    @Test
    @DisplayName("Mapper should not add calculation field when no source entries in report")
    public void shouldNotMapCalculationFieldWhenNoSourceFieldsGiven() {
        final TimeSeriesPoint result = mapper.map(singletonList(createKontaktregisterField("None existing", "42")), now());

        assertEquals(result.getMeasurements().size(), 0);
    }

    @Test
    @DisplayName("Mapper should calculate single combination field into desired element")
    public void shouldMapSingleCalculationElementIntoCalculationFieldInResult() {
        final Measurement result = mapper.map(singletonList(createKontaktregisterField(D5_5.getKrrField(), "42")), now()).getMeasurements().get(0);

        assertAll(() -> assertEquals(result.getId(), D5_5_6.getStatisticId()),
                  () -> assertEquals(result.getValue(), 42));
    }

    @Test
    @DisplayName("Mapper should not fail when all elements are filtered away from result")
    public void shouldNotFailWhenResultsFromMapIsEmpty() {
        final TimeSeriesPoint result = mapper.map(singletonList(createKontaktregisterField("None existing", "42")), now());

        assertEquals(result.getMeasurements().size(), 0);
    }

    @Test
    @DisplayName("Mapper should calculate combination fields")
    public void shouldMapCalculationFieldsWhenAvailable() {
        final Measurement result = mapper.map(asList(
                createKontaktregisterField(D5_5.getKrrField(), "10"),
                createKontaktregisterField(D5_6.getKrrField(), "10")
        ), now()).getMeasurements().get(0);

        assertAll(() -> assertEquals(result.getId(), D5_5_6.getStatisticId()),
                  () -> assertEquals(result.getValue(), 20));
    }

    @Test
    @DisplayName("Mapper should contain all desired fields with values in output")
    public void shouldMapFieldsWhenAvailable() {
        final List<Measurement> result = mapper.map(createValidKontaktregisterFieldListWithAllElements(), now()).getMeasurements();

        assertAll(
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D5_1.getStatisticId())).findFirst().get().getValue(), 1L),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D5_2.getStatisticId())).findFirst().get().getValue(), 2L),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D5_5_6.getStatisticId())).findFirst().get().getValue(), 12L),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D5_7.getStatisticId())).findFirst().get().getValue(), 16L),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D7_4.getStatisticId())).findFirst().get().getValue(), 32L),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D7_5.getStatisticId())).findFirst().get().getValue(), 64L),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D7_6.getStatisticId())).findFirst().get().getValue(), 256L)
        );
    }

    @Test
    @DisplayName("Mapper should not map source fields for calculation")
    public void shouldNotMapSourceFieldsForCalculationsWhenAvailable() {
        final List<Measurement> result = mapper.map(asList(
                createKontaktregisterField(D5_5.getKrrField(), "10"),
                createKontaktregisterField(D5_6.getKrrField(), "10")
        ), now()).getMeasurements();

        assertAll(() -> assertEquals(result.size(), 1),
                  () -> assertNotEquals(result.get(0).getId(), D5_5.getStatisticId()),
                  () -> assertNotEquals(result.get(0), D5_6.getStatisticId()));
    }

    @Test
    @DisplayName("Mapper should contain all desired fields and discard calculation source fields and extra fields")
    public void shouldMapAllDesiredFiledsAndNotMapCalcSourcesOrUnwantedFields() {
        List<KontaktregisterField> elements = new ArrayList<>(createValidKontaktregisterFieldListWithAllElements());
        elements.add(createKontaktregisterField("Not valid field", "something", "ho ho ho", "1069"));
        elements.add(createKontaktregisterField("Winter is coming", "1072"));
        elements.add(createKontaktregisterField("Christmas", "Soon", "1009"));

        final List<Measurement> result = mapper.map(elements, now()).getMeasurements();

        assertAll(
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D5_5.getStatisticId())).findFirst(), Optional.empty()),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals(D5_6.getStatisticId())).findFirst(), Optional.empty()),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals("ChristmasSoon")).findFirst(), Optional.empty()),
                () -> assertEquals(result.stream().filter(e -> e.getId().equals("Winter is coming")).findFirst(), Optional.empty()),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == 1072L).findFirst(), Optional.empty()),
                () -> assertEquals(result.stream().filter(e -> e.getValue() == 1069L).findFirst(), Optional.empty())
        );
    }

    private static List<KontaktregisterField> createValidKontaktregisterFieldListWithAllElements() {
        return asList(
                createKontaktregisterField(D5_1.getKrrField(), "1"),
                createKontaktregisterField(D5_2.getKrrField(), "2"),
                createKontaktregisterField(D5_5.getKrrField(), "4"),
                createKontaktregisterField(D5_6.getKrrField(), "8"),
                createKontaktregisterField(D5_7.getKrrField(), "16"),
                createKontaktregisterField(D7_4.getKrrField(), "32"),
                createKontaktregisterField(D7_5.getKrrField(), "64"),
                createKontaktregisterField(D7_6.getKrrField(), "256")
        );
    }
}