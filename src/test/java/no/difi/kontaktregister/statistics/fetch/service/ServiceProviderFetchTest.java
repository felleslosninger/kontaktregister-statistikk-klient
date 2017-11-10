package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.domain.ServiceProvider;
import no.difi.kontaktregister.statistics.testutils.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderFetchTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ServiceProviderFetch serviceProviderFetch;

    @Test
    public void shouldReturnEmptyListWhenResponseIsEmpty() {
        ServiceProvider[] spList = {};

        when(restTemplate.getForObject(any(URI.class), eq(ServiceProvider[].class))).thenReturn(spList);
        List actualServiceproviders = serviceProviderFetch.perform();
        assertEquals(actualServiceproviders, Arrays.asList(spList));
    }

    @Test
    public void shouldReturnTwoServiceProvidersWhenResponseContainsTwo() {
        ServiceProvider sp1 = new ServiceProvider("entity1", "1");
        ServiceProvider sp2 = new ServiceProvider("entity2", "2");
        ServiceProvider[] spList = {sp1, sp2};

        when(restTemplate.getForObject(any(URI.class), eq(ServiceProvider[].class))).thenReturn(spList);
        List actualServiceproviders = serviceProviderFetch.perform();
        assertEquals(actualServiceproviders, Arrays.asList(spList));
    }
}

