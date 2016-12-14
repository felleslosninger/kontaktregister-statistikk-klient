package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.context.SpringExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

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

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {Application.class, Config.class})
    @SpringBootTest({"url.base.kontaktregister=http://admin-test1.difi.eon.no"})
    @DisplayName("When reading url.base.statistikk property")
    class FailWithExceptionWhenStatistikkUrlPropertyNotPresent {
        @org.junit.Test(expected = ArgumentMissing.class)
        @DisplayName("Should fail startup if one of the arguments are not url.base.kontaktregister")
        public void shouldFailWithArgumentMissingExceptionWhenUrlBaseKontaktregisterNotPresent() {}
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {Application.class, Config.class})
    @SpringBootTest({"url.base.statistikk=http://test-statistikk-inndata.difi.no"})
    @DisplayName("When reading url.base.kontaktregister property")
    class FailWithExceptionWhenKontaktregisterUrlPropertyNotPresent {
        @org.junit.Test(expected = ArgumentMissing.class)
        @DisplayName("Should fail startup if one of the arguments are not url.base.kontaktregister")
        public void shouldFailWithArgumentMissingExceptionWhenUrlBaseStatistikkNotPresent() {}
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {Application.class, Config.class})
    @SpringBootTest({"url.base.statistikk=http://test-statistikk-inndata.difi.no", "url.base.kontaktregister=http://admin-test1.difi.eon.no"})
    @DisplayName("When reading url.base.kontaktregister property")
    class SucceedWhenPropertiesForStatistikkAndKontaktregisterArePresent {
        @org.junit.Test(expected = ArgumentMissing.class)
        @DisplayName("Should fail startup if one of the arguments are not url.base.kontaktregister")
        public void shouldSucceedWhenBothPropertiesForKontaktregisterAndStatistikkArePresent() {}
    }
}