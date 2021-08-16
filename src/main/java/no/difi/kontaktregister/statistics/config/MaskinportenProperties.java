package no.difi.kontaktregister.statistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Validated
@ConfigurationProperties("maskinporten")
@Configuration
public class MaskinportenProperties {
    @NotNull
    private String iss;

    @NotNull
    private String aud;

    @NotNull
    private String tokenEndpoint;

    @NotNull
    private String scope;

    private String kid;

    @Min(1)
    private int connectTimeout;

    @Min(1)
    private int readTimeout;

    @Min(1)
    private int maxConnections;

    @Min(1)
    private int maxConnectionsPerRoute;

    @Valid
    private KeyProperties clientKeys;

    @Data
    public static class KeyProperties {
        @NotNull
        private Resource certificatePath;
        @NotNull
        private Resource privateKeyPath;
    }
}
