package no.difi.kontaktregister.statistics.push.mapper;

class MapperError extends RuntimeException {
    MapperError(String message) {
        super(message);
    }

    MapperError(String message, Throwable exception) {
        super(message, exception);
    }
}
