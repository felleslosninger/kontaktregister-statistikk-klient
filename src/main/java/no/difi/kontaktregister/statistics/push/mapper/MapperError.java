package no.difi.kontaktregister.statistics.push.mapper;

public class MapperError extends RuntimeException {
    public MapperError(String message) {
        super(message);
    }

    public MapperError(String message, Throwable exception) {
        super(message, exception);
    }
}
