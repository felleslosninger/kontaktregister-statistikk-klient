package no.difi.kontaktregister.statistics.push.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Given enum for report D7")
public class ReportD7Test {
    @Test
    @DisplayName("Report should have seven columns")
    public void assureThatReportHasSevenEntries() {
        assertEquals(7, ReportD7.values().length);
    }

    @Test
    @DisplayName("Last column for report should always be sum")
    public void assureThatLastReportColumnIsSum() {
        assertEquals("Sum:", ReportD7.values()[ReportD7.values().length - 1].mapVal());
    }

    @Test
    @DisplayName("Should throw ReportEnumNotFound when value for enum is not found")
    public void shouldThrowReportEnumNotFoundWhenValueForEnumIsNotFound_duh() {
        assertThrows(ReportEnumNotFound.class, () -> ReportD7.fromString("NotEnum"));
    }

    @Test
    @DisplayName("Should map to value when string value for enum is found")
    public void shouldMapToValueWhenStringComparisonValueForEnumIsFound() {
        assertEquals(ReportD7.D7_4, ReportD7.fromString("Aktive postbokser958935420"));
    }

    @Test
    @DisplayName("Should map id to name lowercase")
    public void shouldMapToLowercaseWhenId() {
        assertEquals(ReportD7.D7_6.id(), "d7_6");
    }
}