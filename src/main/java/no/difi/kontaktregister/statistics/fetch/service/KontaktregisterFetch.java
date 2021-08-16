package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.Properties;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import static java.lang.System.currentTimeMillis;

@Component
public class KontaktregisterFetch {
    private final UriTemplate uriTemplate;
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public KontaktregisterFetch(Properties properties, RestTemplate restTemplate) {
        this.uriTemplate = new UriTemplate(
                properties.getIdportenAdminUrl() +
                "/idporten-admin/statistics/statistics/json/" +
                "{reportType}/" +
                "{fromYear}/{fromMonth}/{fromDay}/{fromHour}/" +
                "to/{toYear}/{toMonth}/{toDay}/{toHour}"
        );
        this.restTemplate = restTemplate;
    }

    public List<KontaktregisterField> perform(String reportType, ZonedDateTime from, ZonedDateTime to) {
        URI uri = uriTemplate.expand(reportType, from.getYear(), from.getMonthValue(), from.getDayOfMonth(), from.getHour(), to.getYear(), to.getMonthValue(), to.getDayOfMonth(), to.getHour());
        logger.info("Fetching from " + uri + "...");
        long t0 = currentTimeMillis();
        List<KontaktregisterField> result = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<KontaktregisterField>>(){}
        ).getBody();
        logger.info(result.size() + " fields fetched in " + ((currentTimeMillis() - t0) / 1000) + " seconds");
        return result;
    }
}