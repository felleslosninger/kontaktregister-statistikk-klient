package no.difi.kontaktregister.statistics.config;

import lombok.Getter;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Loads keys from disk..
 */
@Getter
@Service
public class KeySecretProvider {

    private final Certificate certificate;
    private final RSAPrivateKey privateKey;

    private final MaskinportenProperties maskinportenProperties;

    @Autowired
    public KeySecretProvider(MaskinportenProperties properties) {
        this.maskinportenProperties = properties;
        this.privateKey = readPrivateKey(properties.getClientKeys().getPrivateKeyPath());
        this.certificate = readCertificate(properties.getClientKeys().getCertificatePath());
    }


    public Certificate readCertificate(Resource file) {
        CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("Failed to create certFactory for read X.509 certificate for Maskinporten from Docker secrets: " + file.getFilename(), e);
        }

        X509Certificate cer;
        try {
            InputStream stream = file.getInputStream();
            cer = (X509Certificate) certFactory.generateCertificate(stream);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException("Failed to read certificate for Maskinporten from Docker secrets: " + file.getFilename(), e);
        }

        return cer;
    }


    public RSAPrivateKey readPrivateKey(Resource file) {
        String keyAsText;
        try {
            final File keyFile = file.getFile();
            keyAsText = new String(Files.readAllBytes(keyFile.toPath()), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read private key for Maskinporten from Docker secrets: " + file.getFilename(), e);
        }

        String privateKeyPEM = keyAsText
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        final PrivateKey privateKey;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to read private key for Maskinporten from Docker secrets: " + file.getFilename(), e);
        }
        return (RSAPrivateKey) privateKey;
    }

}
