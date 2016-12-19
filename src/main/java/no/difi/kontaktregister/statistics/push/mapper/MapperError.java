package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.exception.KontaktregisterStatisticsError;

class MapperError extends KontaktregisterStatisticsError {
    MapperError(String message) {
        super(message);
    }
}
