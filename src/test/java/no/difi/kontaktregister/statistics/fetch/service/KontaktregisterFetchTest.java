package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.configuration.Application;
import no.difi.kontaktregister.statistics.configuration.Config;
import no.difi.kontaktregister.statistics.context.SpringExtension;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
import no.difi.kontaktregister.statistics.testutils.FileCreatorUtil;
import no.difi.kontaktregister.statistics.testutils.RestServiceMockFactory;
import no.difi.kontaktregister.statistics.util.KontaktregisterReportType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.filename;
import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.filepath;
import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.firstPath;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class, Config.class})
@SpringBootTest({
        "file.base.difi-statistikk=secret",
        "url.base.kontaktregister=https://admin-test1.difi.eon.no",
        "url.base.ingest.statistikk=http://test-statistikk-inndata.difi.no"})
@DisplayName("Reading kontaktregister data")
public class KontaktregisterFetchTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KontaktregisterFetch service;

    private String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    private ZonedDateTime from = ZonedDateTime.now();
    private ZonedDateTime to = ZonedDateTime.now().minusDays(2);

    @BeforeEach
    public void setUp() throws IOException {
        final File file = FileCreatorUtil.createPasswordFileAndPath("someSecret", basePath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileCreatorUtil.removeFile(basePath + filepath + filename);
        FileCreatorUtil.removePath(basePath + filepath);
        FileCreatorUtil.removePath(basePath + firstPath);    }

    @Nested
    @DisplayName("When reading from report D5")
    class ReportD5 {
        @Test
        @DisplayName("Media-type is not application/json, fail with RestClientException")
        public void shouldGetRestClientExceptionWhenWrongMediatypeFromD5() {
            createMockRestServiceServer(MediaType.TEXT_PLAIN, D5, Datasize.sample);

            assertThrows(RestClientException.class, () -> service.perform(D5.getId(), from, to));
        }

        @Test
        @DisplayName("Got data from D5")
        public void shouldRetrieveDataFromD5WhenRequestingReportOnSpesificTime() {
            final MockRestServiceServer server = createMockRestServiceServer(MediaType.APPLICATION_JSON, D5, Datasize.sample);

            KontaktregisterField[] consumer = service.perform(D5.getId(), from, to);
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(  () -> assertEquals(field.size(), 2),
                    () -> assertEquals(field.get(0).getValue(), "Aktive brukere med e-post"));
        }

        @Test
        @DisplayName("It should handle dataset over larger period")
        public void shouldHandleDatasetFromD5WhenPeriodReturnsMoreThanSingleDataset() {
            final MockRestServiceServer server = createMockRestServiceServer(MediaType.APPLICATION_JSON, D5, Datasize.full);

            KontaktregisterField[] consumer = service.perform(D5.getId(), from, to);
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(  () -> assertEquals(field.size(), 3),
                    () -> assertEquals(field.get(0).getValue(), "Aktive brukere med e-post"));
        }
    }

    @Nested
    @DisplayName("When reading from report D7")
    class ReportD7 {
        @Test
        @DisplayName("Media-type is not application/json, fail with RestClientException")
        public void shouldGetRestClientExceptionWhenWrongMediatypeFromD7() {
            createMockRestServiceServer(MediaType.TEXT_PLAIN, D7, Datasize.sample);

            assertThrows(RestClientException.class, () -> service.perform(D7.getId(), from, to));
        }

        @Test
        @DisplayName("Got data from D7")
        public void shouldRetrieveDataFromD7WhenRequestingReportOnSpesificTime() {
            final MockRestServiceServer server = createMockRestServiceServer(MediaType.APPLICATION_JSON, D7, Datasize.sample);

            KontaktregisterField[] consumer = service.perform(D7.getId(), from, to);
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(() -> assertEquals(field.size(), 4),
                    () -> assertEquals(field.get(0).getValue(), "Inaktive postbokser"));
        }

        @Test
        @DisplayName("It should handle dataset over larger period")
        public void shouldHandleDatasetFromD7WhenPeriodReturnsMoreThanSingleDataset() {
            final MockRestServiceServer server = createMockRestServiceServer(MediaType.APPLICATION_JSON, D7, Datasize.full);

            KontaktregisterField[] consumer = service.perform(D7.getId(), from, to);
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(() -> assertEquals(field.size(), 5),
                    () -> assertEquals(field.get(0).getValue(), "Inaktive postbokser"));
        }
    }

    private MockRestServiceServer createMockRestServiceServer(MediaType mediaType, KontaktregisterReportType report, Datasize datasize) {
        return RestServiceMockFactory.createMockRestServiceServer(
                from,
                to,
                mediaType,
                restTemplate,
                report.getId(),
                expectedJson(report, datasize)
        );
    }

    private static String expectedJson(KontaktregisterReportType report, Datasize datasize) {
        if (datasize == Datasize.sample) {
            if (report == KontaktregisterReportType.D5) {
                return "[{\"fields\":[{\"value\":\"Aktive brukere med e-post\"},{\"value\":20080}]}]";
            } else { //D7
                return "[{\"fields\":[{\"value\":\"Inaktive postbokser\"},{\"value\":\"\"},{\"value\":\"958935420\"},{\"value\":10}]}]";
            }
        }
        else {
            if (report == KontaktregisterReportType.D5) {
                return "[{\"fields\":[{\"value\":\"Aktive brukere med e-post\"},{\"value\":20081},{\"value\":20081}]}," +
                        "{\"fields\":[{\"value\":\"Aktive brukere med mobil\"},{\"value\":19861},{\"value\":19861}]}," +
                        "{\"fields\":[{\"value\":\"Aktive brukere med e-post og mobil\"},{\"value\":19843},{\"value\":19843}]}," +
                        "{\"fields\":[{\"value\":\"Aktive brukere med e-post og/eller mobil\"},{\"value\":20099},{\"value\":20099}]}," +
                        "{\"fields\":[{\"value\":\"Aktive brukere med reservasjon og e-post eller mobil\"},{\"value\":39},{\"value\":39}]}," +
                        "{\"fields\":[{\"value\":\"Aktive brukere med reservasjon uten verken e-post eller mobil\"},{\"value\":16},{\"value\":16}]}," +
                        "{\"fields\":[{\"value\":\"Antall aktive brukere i kontaktregisteret\"},{\"value\":20118},{\"value\":20118}]}," +
                        "{\"fields\":[{\"value\":\"Antall slettede brukere i kontaktregisteret\"},{\"value\":4927},{\"value\":4927}]}," +
                        "{\"fields\":[{\"value\":\"Toalt antall brukere i kontaktregisteret\"},{\"value\":25045},{\"value\":25045}]}," +
                        "{\"fields\":[{\"value\":\"Antall brukere som har blitt varslet\"},{\"value\":36},{\"value\":36}]}," +
                        "{\"fields\":[{\"value\":\"Antall brukere som har utløpt\"},{\"value\":15345},{\"value\":15345}]}]";
            }
            else {
                return "[{\"fields\":[{\"value\":\"Inaktive postbokser\"},{\"value\":\"\"},{\"value\":\"958935420\"},{\"value\":10},{\"value\":10}]}," +
                        "{\"fields\":[{\"value\":\"Inaktive postbokser\"},{\"value\":\"Digipost\"},{\"value\":\"984661185\"},{\"value\":20},{\"value\":20}]}," +
                        "{\"fields\":[{\"value\":\"Inaktive postbokser\"},{\"value\":\"eBoks\"},{\"value\":\"996460320\"},{\"value\":15},{\"value\":15}]}," +
                        "{\"fields\":[{\"value\":\"Aktive postbokser\"},{\"value\":\"\"},{\"value\":\"958935420\"},{\"value\":32},{\"value\":32}]}," +
                        "{\"fields\":[{\"value\":\"Aktive postbokser\"},{\"value\":\"Digipost\"},{\"value\":\"984661185\"},{\"value\":17},{\"value\":17}]}," +
                        "{\"fields\":[{\"value\":\"Aktive postbokser\"},{\"value\":\"eBoks\"},{\"value\":\"996460320\"},{\"value\":140},{\"value\":140}]}," +
                        "{\"fields\":[{\"value\":\"Sum:\"},{\"value\":\"\"},{\"value\":\"\"},{\"value\":234},{\"value\":234}]}]";
            }
        }
    }

    private enum Datasize {
        sample,
        full
    }
}