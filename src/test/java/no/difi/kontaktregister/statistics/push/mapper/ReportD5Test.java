package no.difi.kontaktregister.statistics.push.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Given enum for report D5")
public class ReportD5Test {
    @Test
    @DisplayName("Report should have eleven columns")
    public void assureThatReportHasElevenEntries() {
        assertEquals(11, ReportD5.values().length);
    }

    @Test
    @DisplayName("Should throw ReportEnumNotFound when value for enum is not found")
    public void shouldThrowReportEnumNotFoundWhenValueForEnumIsNotFound_duh() {
        assertThrows(ReportEnumNotFound.class, () -> ReportD5.fromString("NotEnum"));
    }

    @Test
    @DisplayName("Should map to value when string value for enum is found")
    public void shouldMapToValueWhenStringComparisonValueForEnumIsFound() {
        assertEquals(ReportD5.D5_9, ReportD5.fromString("Toalt antall brukere i kontaktregisteret"));
    }

    @Test
    @DisplayName("Should map id to name lowercase")
    public void shouldMapToLowercaseWhenId() {
        assertEquals(ReportD5.D5_4.id(), "d5_4");
    }
}