package no.difi.kontaktregister.statistics.testutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCreatorUtil {
    public final static String filename = "/krr-stat-pumba";
    public final static String firstPath = "/run";
    public final static String filepath = firstPath + "/secrets";

    public static File createPasswordFileAndPath(String pwd, String basePath) throws IOException {
        File file;
        try {
            file = new File(basePath);
            final Path path = Paths.get(file.getPath() + filepath);
            final Path tempFile = Paths.get(file.getPath() + filepath + filename);
            Files.createDirectories(path);
            Files.createFile(tempFile);
            Files.write(tempFile, pwd.getBytes());
        } catch (Exception e) {
            return new File(basePath + filepath + filename);
        }
        return file;
    }

    public static void removeFile(String filename) throws IOException {
        final File file = new File(filename);
        final Path tempFile = Paths.get(file.getPath());
        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    public static void removePath(String pathTip) throws IOException {
        final File path = new File(pathTip);
        final Path tempPath = Paths.get(path.getPath());
        if (Files.exists(tempPath) && Files.isDirectory(tempPath)) {
            Files.delete(tempPath);
        }
    }
}
