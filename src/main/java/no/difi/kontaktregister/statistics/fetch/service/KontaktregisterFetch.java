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
//        final String urlTemplate = "http://eid-systest-admin01.dmz.local:10006/idporten-admin/statistics/statistics/json/{report}/{year}/{month}/{day}/{hour}";
        final String urlTemplate = "https://admin-test1.difi.eon.no/idporten-admin/statistics/statistics/json/{report}/{year}/{month}/{day}/{hour}";
        return restTemplate.getForObject(
                urlTemplate,
                KontaktregisterFields[].class, report, dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour()
        );
    }
}