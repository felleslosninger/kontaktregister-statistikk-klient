package no.difi.kontaktregister.statistics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceProvider {

    @JsonProperty("EntityId")
    private String entityId;

    @JsonProperty("Organisasjonsnummer")
    private String organisasjonsnummer;

    public ServiceProvider(String entityId, String organisasjonsnummer){
        this.entityId = entityId;
        this.organisasjonsnummer = organisasjonsnummer;

    }

}
