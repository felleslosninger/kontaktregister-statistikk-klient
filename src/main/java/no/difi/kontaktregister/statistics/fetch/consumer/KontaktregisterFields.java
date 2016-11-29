package no.difi.kontaktregister.statistics.fetch.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KontaktregisterFields {
    @JsonProperty("fields")
    List<KontaktregisterValue> values;

    public List<KontaktregisterValue> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }
        return values;
    }
}
