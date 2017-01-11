package no.difi.kontaktregister.statistics.configuration;

import no.difi.kontaktregister.statistics.exception.KontaktregisterStatisticsError;

import java.net.MalformedURLException;

public class InvalidUrl extends KontaktregisterStatisticsError {
    public InvalidUrl(String message, MalformedURLException cause) {
        super(message, cause);
    }
}
