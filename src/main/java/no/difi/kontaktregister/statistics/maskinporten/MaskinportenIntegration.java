package no.difi.kontaktregister.statistics.maskinporten;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import no.difi.kontaktregister.statistics.config.KeySecretProvider;
import no.difi.kontaktregister.statistics.config.MaskinportenProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Integrates with maskinporten to acquire access tokens using JWT bearer grants.
 */
@Slf4j
@Service
public class MaskinportenIntegration {

    private final MaskinportenProperties maskinportenProperties;

    private final RestTemplate restTemplate;

    private final KeySecretProvider keys;

    private volatile String currentAccessToken;

    private volatile Long expiresIn;

    public MaskinportenIntegration(MaskinportenProperties maskinportenProperties, RestTemplate restTemplate, KeySecretProvider keys) {
        this.maskinportenProperties = maskinportenProperties;
        this.restTemplate = restTemplate;
        this.keys = keys;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String acquireAccessToken() {
        if (currentAccessToken != null) {
            return currentAccessToken;
        }
        return acquireNewAccessToken();
    }

    public String acquireNewAccessToken() {
        String jwt = makeJwt();
        TokenResponse tokenResponse = callTokenEndpoint(maskinportenProperties.getTokenEndpoint(), jwt);
        currentAccessToken = tokenResponse.getAccessToken();
        expiresIn = tokenResponse.getExpiresIn();
        final long expiresTimeInMs = Clock.systemUTC().millis() + (expiresIn * 1000);
        log.info("Successfully fetched access-token from Maskinporten: " + maskinportenProperties.getTokenEndpoint() + ". Expires: " + new Date(expiresTimeInMs) + " (Epoch:" + expiresTimeInMs + ")");
        return currentAccessToken;
    }

    private String makeJwt() {
        try {
            JWSHeader jwtHeader;
            if (ObjectUtils.isEmpty(maskinportenProperties.getKid())) {
                List<Base64> certChain = new ArrayList<>();
                certChain.add(Base64.encode(keys.getCertificate().getEncoded()));
                jwtHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(certChain).build();
            } else {
                jwtHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(maskinportenProperties.getKid()).build();
            }
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .audience(maskinportenProperties.getAud())
                    .issuer(maskinportenProperties.getIss())
                    .claim("scope", maskinportenProperties.getScope())
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(new Date(Clock.systemUTC().millis()))
                    .expirationTime(new Date(Clock.systemUTC().millis() + 120000)) // Expiration time is 120 sec.
                    .build();
            JWSSigner signer = new RSASSASigner(keys.getPrivateKey());
            SignedJWT signedJWT = new SignedJWT(jwtHeader, claims);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Couldn't create JWT " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private TokenResponse callTokenEndpoint(String tokenEndpoint, String assertion) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        map.add("assertion", assertion);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForObject(tokenEndpoint, request, TokenResponse.class);
    }
}
