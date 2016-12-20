package no.difi.kontaktregister.statistics.util;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadSecret {
    public static String getPwd(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            throw new UtilError(HttpStatus.NOT_ACCEPTABLE.getReasonPhrase());
        }
    }
}
