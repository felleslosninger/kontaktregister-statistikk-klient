package no.difi.kontaktregister.statistics.testutils;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RestServiceMockFactory {
    public static MockRestServiceServer createMockRestServiceServer(LocalDateTime dateTime, MediaType mediaType, RestTemplate restTemplate, String report, String expected) {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(ExpectedCount.once(),
                requestTo("https://admin-test1.difi.eon.no/idporten-admin/statistics/statistics/json/" + report + "/"
                        + dateTime.getYear() + "/"
                        + dateTime.getMonthValue() + "/"
                        + dateTime.getDayOfMonth() + "/"
                        + dateTime.getHour()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expected, mediaType));

        return server;
    }
}
