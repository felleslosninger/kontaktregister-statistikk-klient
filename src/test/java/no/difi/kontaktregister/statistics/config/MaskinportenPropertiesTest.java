package no.difi.kontaktregister.statistics.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("When fetching MaskinportenProperties")
public class MaskinportenPropertiesTest {

    @Autowired
    private  MaskinportenProperties mp;

    @Test
    @DisplayName("then scope property from application.yml is not null")
    public void testFetchScope(){
        assertNotNull(mp.getScope(), "Scope property missing");
    }

    @Test
    @DisplayName("then kid property is null since should be fetched from docker config instead")
    public void testKidIsNull(){
        assertNull(mp.getKid(), "Kid property should not be found in application.yml, but in docker config which is not configured in this test");
    }
}
