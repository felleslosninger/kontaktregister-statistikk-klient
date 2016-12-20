package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.exception.KontaktregisterStatisticsError;

class ArgumentMissing extends KontaktregisterStatisticsError {
    ArgumentMissing(String message) {
        super(message);
    }

    ArgumentMissing(String message, Throwable cause) {
        super(message, cause);
    }
}
