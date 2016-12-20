package no.difi.kontaktregister.statistics.util;

import no.difi.kontaktregister.statistics.exception.KontaktregisterStatisticsError;

class UtilError extends KontaktregisterStatisticsError {
    UtilError(String message) {
        super(message);
    }
}
