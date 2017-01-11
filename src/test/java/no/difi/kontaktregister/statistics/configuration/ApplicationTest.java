package no.difi.kontaktregister.statistics.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("When starting the application")
public class ApplicationTest {
    @Test
    @DisplayName("Should fail and exit if no arguments is given")
    public void shouldFailWithArgumentMissingExceptionWhenNoArgumentIsGiven() {
        assertThrows(ArgumentMissing.class, () -> Application.main(null));
    }

    @Test
    @DisplayName("Should fail and exit if only one argument is given")
    public void shouldFailWithArgumentMissingExceptionWhenOneArgumentIsGiven() {
        assertThrows(ArgumentMissing.class, () -> Application.main(new String[]{"url"}));
    }

}