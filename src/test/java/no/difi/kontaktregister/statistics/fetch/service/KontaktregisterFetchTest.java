package no.difi.kontaktregister.statistics.fetch.service;

import no.difi.kontaktregister.statistics.testutils.FileCreatorUtil;
import no.difi.kontaktregister.statistics.testutils.RestServiceMockFactory;
import no.difi.kontaktregister.statistics.configuration.Application;
import no.difi.kontaktregister.statistics.configuration.Config;
import no.difi.kontaktregister.statistics.context.SpringExtension;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.filename;
import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.filepath;
import static no.difi.kontaktregister.statistics.testutils.FileCreatorUtil.firstPath;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D5;
import static no.difi.kontaktregister.statistics.util.KontaktregisterReportType.D7;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class, Config.class})
@SpringBootTest({
        "url.base.kontaktregister=http://admin-test1.difi.eon.no",
        "url.base.statistikk=http://test-statistikk-inndata.difi.no",
        "file.base.difi-statistikk=c:/projects/kontaktregister-statistikk-klient/target/test-classes/run/secrets/krr-stat-pumba"})
@DisplayName("Reading kontaktregister data")
public class KontaktregisterFetchTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KontaktregisterFetch service;

    private String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

    @BeforeEach
    public void setUp() throws IOException {
        FileCreatorUtil.createPasswordFileAndPath("secret", basePath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileCreatorUtil.removeFile(basePath + filepath + filename);
        FileCreatorUtil.removePath(basePath + filepath);
        FileCreatorUtil.removePath(basePath + firstPath);
    }

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

            KontaktregisterField[] consumer = service.perform(D5.getId(), currentTime.atZone(ZoneId.systemDefault()));
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

            KontaktregisterField[] consumer = service.perform(D7.getId(), currentTime.atZone(ZoneId.systemDefault()));
            List<KontaktregisterValue> field = consumer[0].getValues();

            server.verify();
            assertAll(  () -> assertEquals(field.size(), 4),
                        () -> assertEquals(field.get(0).getValue(), "Inaktive postbokser"));
        }
    }

    private MockRestServiceServer createMockRestServiceServer(LocalDateTime dateTime, MediaType mediaType, KontaktregisterReportType report) {
        return RestServiceMockFactory.createMockRestServiceServer(
                dateTime,
                mediaType,
                restTemplate,
                report.getId(),
                expectedJson(report)
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