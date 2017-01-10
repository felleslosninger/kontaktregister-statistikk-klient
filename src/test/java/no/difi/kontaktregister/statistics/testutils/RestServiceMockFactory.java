package no.difi.kontaktregister.statistics.testutils;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RestServiceMockFactory {
    public static MockRestServiceServer createMockRestServiceServer(ZonedDateTime from, ZonedDateTime to, MediaType mediaType, RestTemplate restTemplate, String report, String expected) {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(ExpectedCount.once(),
                requestTo("https://admin-test1.difi.eon.no/idporten-admin/statistics/statistics/json/" + report + "/"
                        + from.getYear() + "/"
                        + from.getMonthValue() + "/"
                        + from.getDayOfMonth() + "/"
                        + from.getHour() + "/to/"
                        + to.getYear() + "/"
                        + to.getMonthValue() + "/"
                        + to.getDayOfMonth() + "/"
                        + to.getHour()
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expected, mediaType));

        return server;
    }
}
