package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.util.KontaktregisterReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.ZonedDateTime;

import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@DisplayName("When reading kontaktregister data")
public class KontaktregisterFetchTest {
    private KontaktregisterFetch service;

    @Mock
    private RestTemplate restTemplateMock;

    @BeforeEach
    public void setUp() throws IOException {
        initMocks(this);

        service = new KontaktregisterFetch(restTemplateMock);
    }

    @Test
    @DisplayName("It should get RestClientException when it fails")
    public void shouldGetRestClientExceptionWhenReturnedIsNotJson() {
        when(restTemplateMock.getForObject(anyString(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject())).thenThrow(new RestClientException(""));

        service.perform("d5", ZonedDateTime.now());
    }

    @Test
    @DisplayName("It should pass on correct parameters and succeed when all parameters are available")
    public void shouldPassParametersToIngestClientWhenAllParametersAreCorrect() {
        when(restTemplateMock.getForObject(anyString(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(expectedJson(D7));
        final ZonedDateTime now = ZonedDateTime.now();
        ArgumentCaptor<String> captureVararg = ArgumentCaptor.forClass(String.class);

        service.perform(D7.getId(), now);

        verify(restTemplateMock, times(1)).getForObject(anyString(), anyObject(), captureVararg.capture());

        assertAll(
                () -> assertEquals(D7.getId(), captureVararg.getAllValues().get(0)),
                () -> assertEquals(now.getYear(), captureVararg.getAllValues().get(1)),
                () -> assertEquals(now.getMonthValue(), captureVararg.getAllValues().get(2)),
                () -> assertEquals(now.getDayOfMonth(), captureVararg.getAllValues().get(3)),
                () -> assertEquals(now.getHour(), captureVararg.getAllValues().get(4))
        );
    }

    private static String expectedJson(KontaktregisterReportType report) {
        if (report == KontaktregisterReportType.D5) {
            return "[{\"fields\":[{\"value\":\"Aktive brukere med e-post\"},{\"value\":20080}]}]";
        }
        else { //D7
            return "[{\"fields\":[{\"value\":\"Inaktive postbokser\"},{\"value\":\"\"},{\"value\":\"958935420\"},{\"value\":10}]}]";
        }
    }
}