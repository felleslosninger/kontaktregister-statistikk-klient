package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.RestServiceMockFactory;
import no.difi.kontaktregister.statistics.configuration.Application;
import no.difi.kontaktregister.statistics.configuration.Config;
import no.difi.kontaktregister.statistics.context.SpringExtension;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterFields;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
import no.difi.kontaktregister.statistics.util.ReportType;
import org.junit.Ignore;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static no.difi.kontaktregister.statistics.util.ReportType.D5;
import static no.difi.kontaktregister.statistics.util.ReportType.D7;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class, Config.class})
@DisplayName("Reading kontaktregister data")
public class KontaktregisterFetchTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KontaktregisterFetch service;

    @Nested
    @DisplayName("When reading from report D5")
    class ReportD5 {
        @Test
        @DisplayName("Media-type is not application/json, fail with RestClientException")
        public void shouldGetRestClientExceptionWhenWrongMediatype() {
            LocalDateTime currentTime = LocalDateTime.now();

            createMockRestServiceServer(currentTime, MediaType.TEXT_PLAIN, D5);

            assertThrows(RestClientException.class, () -> service.perform(D5.getId(), currentTime.atZone(ZoneId.systemDefault())));

        }

        @Test
        @DisplayName("Got data from D5")
        public void shouldRetrieveDataWhenRequestingReportOnSpesificTime() {
            LocalDateTime currentTime = LocalDateTime.now();

            MockRestServiceServer server = createMockRestServiceServer(currentTime, MediaType.APPLICATION_JSON, D5);

            KontaktregisterFields[] consumer = service.perform(D5.getId(), currentTime.atZone(ZoneId.systemDefault()));
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(  () -> assertEquals(field.size(), 2),
                        () -> assertEquals(field.get(0).getValue(), "Aktive brukere med e-post"));
        }
    }

    @Nested
    @DisplayName("When reading from report D7")
    class ReportD7 {
        @Test
        @DisplayName("Media-type is not application/json, fail with RestClientException")
        public void shouldGetRestClientExceptionWhenWrongMediatype() {
            LocalDateTime currentTime = LocalDateTime.now();

            createMockRestServiceServer(currentTime, MediaType.TEXT_PLAIN, D7);

            assertThrows(RestClientException.class, () -> service.perform(D7.getId(), currentTime.atZone(ZoneId.systemDefault())));
        }

        @Test
        @DisplayName("Got data from D7")
        public void shouldRetrieveDataWhenRequestingReportOnSpesificTime() {
            LocalDateTime currentTime = LocalDateTime.now();

            MockRestServiceServer server = createMockRestServiceServer(currentTime, MediaType.APPLICATION_JSON, D7);

            KontaktregisterFields[] consumer = service.perform(D7.getId(), currentTime.atZone(ZoneId.systemDefault()));
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(  () -> assertEquals(field.size(), 4),
                        () -> assertEquals(field.get(0).getValue(), "Inaktive postbokser"));
        }
    }

    private MockRestServiceServer createMockRestServiceServer(LocalDateTime dateTime, MediaType mediaType, ReportType report) {
        return RestServiceMockFactory.createMockRestServiceServer(
                dateTime,
                mediaType,
                restTemplate,
                report.getId(),
                expectedJson(report)
        );
    }

    private static String expectedJson(ReportType report) {
        if (report == ReportType.D5) {
            return "[{\"fields\":[{\"value\":\"Aktive brukere med e-post\"},{\"value\":20080}]}]";
        }
        else { //D7
            return "[{\"fields\":[{\"value\":\"Inaktive postbokser\"},{\"value\":\"\"},{\"value\":\"958935420\"},{\"value\":10}]}]";
        }
    }
}