package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterFields;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;

public class KontaktregisterFetch {
    private final RestTemplate restTemplate;

    public KontaktregisterFetch(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public KontaktregisterFields[] perform(String report, ZonedDateTime dateTime) {
        return restTemplate.getForObject(
                "https://admin-test1.difi.eon.no/idporten-admin/statistics/statistics/json/{report}/{year}/{month}/{day}/{hour}",
                KontaktregisterFields[].class, report, dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour()
        );
    }
}