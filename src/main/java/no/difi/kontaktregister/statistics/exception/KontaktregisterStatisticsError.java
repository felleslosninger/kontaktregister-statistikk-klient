package no.difi.kontaktregister.statistics.exception;

import java.net.MalformedURLException;

public class KontaktregisterStatisticsError extends RuntimeException {
    public KontaktregisterStatisticsError(String message) {
        super(message);
    }

    public KontaktregisterStatisticsError(String message, MalformedURLException cause) {
        super(message, cause);
    }
}
