package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;

public class KontaktregisterFetch {
    private final RestTemplate restTemplate;

    public KontaktregisterFetch(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public KontaktregisterField[] perform(String retportType, ZonedDateTime dateTime) {
        final String urlTemplate = "https://admin-test1.difi.eon.no/idporten-admin/statistics/statistics/json/{retportType}/{year}/{month}/{day}/{hour}";
        return restTemplate.getForObject(
                urlTemplate,
                KontaktregisterField[].class, retportType, dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour()
        );
    }
}