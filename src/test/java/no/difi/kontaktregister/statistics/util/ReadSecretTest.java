package no.difi.kontaktregister.statistics.util;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static no.difi.kontaktregister.statistics.util.ReadSecret.getPwd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("When fetching secret then")
public class ReadSecretTest {

    private final static String filename = "/krr-stat-pumba";
    private final static String firstPath = "/run";
    private final static String filepath = firstPath + "/secrets";
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

        final File file = createPasswordFileAndPath(pwd);

        assertEquals(pwd, getPwd(file.getPath() + filepath + filename));
    }

    @AfterEach
    public void tearDown() throws IOException {
        removeFile(filepath + filename);
        removePath(filepath);
        removePath(firstPath);
    }

    private File createPasswordFileAndPath(String pwd) throws IOException {
        final File file = new File(basePath);
        final Path path = Paths.get(file.getPath() + filepath);
        final Path tempFile = Paths.get(file.getPath() + filepath + filename);
        Files.createDirectories(path);
        Files.createFile(tempFile);
        Files.write(tempFile, pwd.getBytes());
        return file;
    }

    private void removeFile(String filename) throws IOException {
        final File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + filename);
        final Path tempFile = Paths.get(file.getPath());
        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    private void removePath(String pathTip) throws IOException {
        final File path = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + pathTip);
        final Path tempPath = Paths.get(path.getPath());
        if (Files.exists(tempPath) && Files.isDirectory(tempPath)) {
            Files.delete(tempPath);
        }
    }
}