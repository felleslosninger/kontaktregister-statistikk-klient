package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.util.UtilError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@DisplayName("When configuration is set up")
public class ConfigTest {
    private Config config;

    private static final String VALID_URL = "http://valid.url.no";
    private static final String INVALID_URL = "not.an.url";

    @Mock private Environment environmentMock;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    @DisplayName("Initialization should fail and application shut down when url.base.kontaktregister is missing")
    public void shouldFailWhenUrlBaseKontaktregisterIsLackingInConfig() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenThrow(new IllegalStateException());
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn("something.else");

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should fail and application shut down when url.base.statistikk is missing")
    public void shouldFailWhenUrlBaseStatistikkIsLackingInConfig() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenThrow(new IllegalStateException());
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn("some.path");

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should fail and application shut down when path.base.difi-statistikk is missing")
    public void shouldFailWhenPathBaseDifiStatistikkIsLackingInConfig() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenThrow(new IllegalStateException());

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should fail when url.base.kontaktregister is not a valid url")
    public void shouldFailWhenUrlBaseKontaktregisterIsNotAValidUrl() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(INVALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn("some.path");

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should fail when url.base.statistikk is not a valid url")
    public void shouldFailWhenUrlBaseStatistikkIsNotAValidUrl() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(INVALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn("some.path");

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should succeed when all parameters are valid")
    public void shouldSucceedWhenParametersAreValid() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn("some.path");

        new Config(environmentMock);
    }

    @Test
    @DisplayName("Should get UtilError when failing to read secret")
    public void shouldGetUtilErrorWhenFailingToReadSecret() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn("some.path");

        assertThrows(UtilError.class, () -> new Config(environmentMock));
    }
}