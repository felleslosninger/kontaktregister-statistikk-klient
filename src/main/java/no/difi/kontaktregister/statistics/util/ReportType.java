package no.difi.kontaktregister.statistics.util;

import static java.lang.String.format;

public enum ReportType {
    D5("d5", "Digital Postkasse", "digitalpostkasse"),
    D7("d7", "Kontakt- og reservasjonsregisteret", "kontaktogreservasjon");

    private final String id;
    private final String name;
    private final String serieId;

    ReportType(String id, String name, String serieId) {
        this.id = id;
        this.name = name;
        this.serieId = serieId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSerieId() {
        return serieId;
    }

    public String getNameWithBracket() {
        return format("[%s]", name);
    }
}