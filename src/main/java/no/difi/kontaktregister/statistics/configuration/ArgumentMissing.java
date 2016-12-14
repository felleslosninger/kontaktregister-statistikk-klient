package no.difi.kontaktregister.statistics.configuration;

class ArgumentMissing extends RuntimeException {
    ArgumentMissing(String message) {
        super(message);
    }

    ArgumentMissing(String message, Throwable cause) {
        super(message, cause);
    }
}
