package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.domain.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class ServiceProviderFetch {
    private final UriTemplate uriTemplate;
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ServiceProviderFetch(URL baseUrl, RestTemplate restTemplate) {
        this.uriTemplate = new UriTemplate(baseUrl +"/idporten-admin/sp/idporten/rest/splist/");
        this.restTemplate = restTemplate;
    }

    public List<ServiceProvider> perform() {
        URI uri = uriTemplate.expand();
        logger.info("Fetching serviceproviders from " + uri + "...");
        long t0 = currentTimeMillis();
        ServiceProvider[] result =  restTemplate.getForObject(uri, ServiceProvider[].class);
        logger.info(result.length + " serviceproviders fetched in " + ((currentTimeMillis() - t0) ) + " ms");
        return Arrays.asList(result);
    }
}
