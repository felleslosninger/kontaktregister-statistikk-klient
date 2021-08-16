package no.difi.kontaktregister.statistics.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("When fetch key-secrets from configuration")
public class KeySecretProviderTest {

    @Mock
    private MaskinportenProperties properties;

    @Mock
    private MaskinportenProperties.KeyProperties keyProperties;


    @Test
    @DisplayName("and files exists then read certificate and private key successfully")
    public void readTestSecretsFromFileIsSuccessful() {
        ClassPathResource certPath = new ClassPathResource("selfmade.cer");
        ClassPathResource privateKeyPath = new ClassPathResource("selfmade-private.pem");

        when(keyProperties.getPrivateKeyPath()).thenReturn(privateKeyPath);
        when(keyProperties.getCertificatePath()).thenReturn(certPath);
        when(properties.getClientKeys()).thenReturn(keyProperties);

        KeySecretProvider keySecretProvider = new KeySecretProvider(properties);
        assertNotNull(keySecretProvider);
        assertNotNull("PrivateKey not read successfully", keySecretProvider.getPrivateKey());
        assertNotNull("Certificate not read successfully", keySecretProvider.getCertificate());
    }

    @Test
    @DisplayName("and private key file is missing then read private key throws RuntimeException")
    public void readMissingPrivateKeyThrowsRuntimeException() {

        ClassPathResource privateKeyPath = new ClassPathResource("doesNotExists-private.pem");

        when(keyProperties.getPrivateKeyPath()).thenReturn(privateKeyPath);
        when(properties.getClientKeys()).thenReturn(keyProperties);

        assertThrows(RuntimeException.class, () -> new KeySecretProvider(properties));
    }
    @Test
    @DisplayName("and certificate file is missing then read certificate throws RuntimeException")
    public void readMissingCertificateThrowsRuntimeException() {

        ClassPathResource privateKeyPath = new ClassPathResource("selfmade-private.pem");
        ClassPathResource certPath = new ClassPathResource("doesNotExists.cer");

        when(keyProperties.getPrivateKeyPath()).thenReturn(privateKeyPath);
        when(keyProperties.getCertificatePath()).thenReturn(certPath);
        when(properties.getClientKeys()).thenReturn(keyProperties);

        assertThrows(RuntimeException.class, () -> new KeySecretProvider(properties));
    }
}
