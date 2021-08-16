package no.difi.kontaktregister.statistics.maskinporten;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


@SpringBootTest
@ActiveProfiles("test")
@DisplayName("When calling Maskinporten")
public class MaskinportenIntegrationTest {

    @Autowired
    private MaskinportenIntegration maskinportenIntegration;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("to fetch accessToken from token-endpoint then accesstoken should not be null")
    public void testMaskinportenIntegration() throws URISyntaxException, JsonProcessingException {

        TestTokenResponse tokenResponse = new TestTokenResponse("my-access-token");

        mockServer.expect(ExpectedCount.once(), requestTo(new URI("https://maskinporten/token"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(tokenResponse))
        );
        final String accessToken = maskinportenIntegration.acquireAccessToken();
        mockServer.verify();
        assertNotNull(accessToken,"Failed get accesstoken");
    }
}
