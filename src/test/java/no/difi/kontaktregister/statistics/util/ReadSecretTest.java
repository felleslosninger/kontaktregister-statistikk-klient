package no.difi.kontaktregister.statistics.util;

import no.difi.kontaktregister.statistics.testutils.FileCreatorUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.*;
import static no.difi.kontaktregister.statistics.util.ReadSecret.getPwd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("When fetching secret then")
public class ReadSecretTest {
    private String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

    @Test
    @DisplayName("Expect UtilError when secret can't be found")
    public void shouldGetUtilErrorWhenNoSecretIsFound() {
        assertThrows(UtilError.class, () -> getPwd("/run/secrets/krr-stat-pumba"));
    }

    @Test
    @DisplayName("Should find and return secret")
    public void shouldFindAndReturnSecretWhenAvailable() throws IOException {
        final String pwd = "someSecret";

        final File file = FileCreatorUtil.createPasswordFileAndPath(pwd, basePath);

        assertEquals(pwd, getPwd(file.getPath() + filepath + filename));
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileCreatorUtil.removeFile(basePath + filepath + filename);
        FileCreatorUtil.removePath(basePath + filepath);
        FileCreatorUtil.removePath(basePath + firstPath);
    }
}