package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.time.ZonedDateTime;

public class KontaktregisterFetch {
    private final URL baseUrl;
    private final RestTemplate restTemplate;

    public KontaktregisterFetch(URL baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public KontaktregisterField[] perform(String retportType, ZonedDateTime from, ZonedDateTime to) {
        final String urlTemplate = baseUrl + "/idporten-admin/statistics/statistics/json/" +
                "{retportType}/" +
                "{fromYear}/{fromMonth}/{fromDay}/{fromHour}/" +
                "to/{toYear}/{toMonth}/{toDay}/{toHour}";
        return restTemplate.getForObject(
                urlTemplate,
                KontaktregisterField[].class, retportType,
                from.getYear(), from.getMonthValue(), from.getDayOfMonth(), from.getHour(),
                to.getYear(), to.getMonthValue(), to.getDayOfMonth(), to.getHour()
        );
    }
}