package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;

import static java.lang.System.currentTimeMillis;

public class KontaktregisterFetch {
    private final UriTemplate uriTemplate;
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public KontaktregisterFetch(URL baseUrl, RestTemplate restTemplate) {
        this.uriTemplate = new UriTemplate(
                baseUrl + "/idporten-admin/statistics/statistics/json/" +
                "{retportType}/" +
                "{fromYear}/{fromMonth}/{fromDay}/{fromHour}/" +
                "to/{toYear}/{toMonth}/{toDay}/{toHour}"
        );
        this.restTemplate = restTemplate;
    }

    public KontaktregisterField[] perform(String reportType, ZonedDateTime from, ZonedDateTime to) {
        URI uri = uriTemplate.expand(reportType, from.getYear(), from.getMonthValue(), from.getDayOfMonth(), from.getHour(), to.getYear(), to.getMonthValue(), to.getDayOfMonth(), to.getHour());
        logger.info("Fetching from " + uri + "...");
        long t0 = currentTimeMillis();
        KontaktregisterField[] result = restTemplate.getForObject(uri, KontaktregisterField[].class);
        logger.info(result.length + " fields fetched in " + ((currentTimeMillis() - t0) / 1000) + " seconds");
        return result;
    }
}