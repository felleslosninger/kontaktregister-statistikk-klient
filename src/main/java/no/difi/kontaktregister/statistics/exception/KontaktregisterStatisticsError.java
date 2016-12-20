package no.difi.kontaktregister.statistics.exception;

public class KontaktregisterStatisticsError extends RuntimeException {
    public KontaktregisterStatisticsError(String message) {
        super(message);
    }

    public KontaktregisterStatisticsError(String message, Throwable cause) {
        super(message, cause);
    }
}
