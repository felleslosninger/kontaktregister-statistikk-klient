package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.testutils.FileCreatorUtil;
import no.difi.kontaktregister.statistics.util.UtilError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;

import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.filename;
import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.filepath;
import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.firstPath;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@DisplayName("When configuration is set up")
public class ConfigTest {
    private static final String VALID_URL = "http://valid.url.no";
    private static final String INVALID_URL = "not.an.url";
    private String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

    @Mock private Environment environmentMock;

    private String file;

    @BeforeEach
    public void setUp() throws IOException {
        initMocks(this);

        file = FileCreatorUtil.createPasswordFileAndPath("secret", basePath).getPath() + filepath + filename;
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileCreatorUtil.removeFile(basePath + filepath + filename);
        FileCreatorUtil.removePath(basePath + filepath);
        FileCreatorUtil.removePath(basePath + firstPath);
    }

    @Test
    @DisplayName("Initialization should fail and application shut down when url.base.kontaktregister is missing")
    public void shouldFailWhenUrlBaseKontaktregisterIsLackingInConfig() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenThrow(new IllegalStateException());
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn(file);

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should fail and application shut down when url.base.statistikk is missing")
    public void shouldFailWhenUrlBaseStatistikkIsLackingInConfig() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenThrow(new IllegalStateException());
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn(file);

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
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn(file);

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should fail when url.base.statistikk is not a valid url")
    public void shouldFailWhenUrlBaseStatistikkIsNotAValidUrl() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(INVALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn(file);

        assertThrows(ArgumentMissing.class, () -> new Config(environmentMock));
    }

    @Test
    @DisplayName("Initialization should succeed when all parameters are valid")
    public void shouldSucceedWhenParametersAreValid() {
        when(environmentMock.getRequiredProperty("url.base.kontaktregister")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("url.base.statistikk")).thenReturn(VALID_URL);
        when(environmentMock.getRequiredProperty("file.base.difi-statistikk")).thenReturn(file);

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