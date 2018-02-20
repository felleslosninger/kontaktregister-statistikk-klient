package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
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
    @DisplayName("Mapper should contain all desired fields with values in output")
    public void shouldMapFieldsWhenAvailable() {
        final Map<String, Long> result = mapper.map(createValidKontaktregisterFieldListWithAllElements(), now()).get(0).getMeasurements();

        assertAll(
                () -> assertEquals(Long.valueOf(1L), result.get(D5_1.getStatisticId())),
                () -> assertEquals(Long.valueOf(2L), result.get(D5_2.getStatisticId())),
                () -> assertEquals(Long.valueOf(12L), result.get(D5_5_6.getStatisticId())),
                () -> assertEquals(Long.valueOf(16L), result.get(D5_7.getStatisticId())),
                () -> assertEquals(Long.valueOf(64L), result.get(D7_3.getStatisticId())),
                () -> assertEquals(Long.valueOf(128L), result.get(D7_4.getStatisticId()))
        );
    }

    @Test
    @DisplayName("Mapper should contain all desired fields and discard calculation source fields and extra fields")
    public void shouldMapAllDesiredFiledsAndNotMapCalcSourcesOrUnwantedFields() {
        List<KontaktregisterField> elements = new ArrayList<>(createValidKontaktregisterFieldListWithAllElements());
        elements.add(createKontaktregisterField("Not valid field", "something", "ho ho ho", "1069"));
        elements.add(createKontaktregisterField("Winter is coming", "1072"));
        elements.add(createKontaktregisterField("Christmas", "Soon", "1009"));

        final Map<String, Long> result = mapper.map(elements, now()).get(0).getMeasurements();

        assertAll(
                () -> assertFalse(result.containsKey("ChristmasSoon")),
                () -> assertFalse(result.containsKey("Winter is coming")),
                () -> assertFalse(result.containsValue(1072L)),
                () -> assertFalse(result.containsValue(1069L))
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

}