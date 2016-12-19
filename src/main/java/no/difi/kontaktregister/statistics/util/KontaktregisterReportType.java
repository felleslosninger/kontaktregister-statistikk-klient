package no.difi.kontaktregister.statistics.util;

import static java.lang.String.format;

public enum KontaktregisterReportType {
    D5("d5", "Kontakt- og reservasjonsregisteret"),
    D7("d7", "Digital Postkasse");

    private final String id;
    private final String name;

    KontaktregisterReportType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getNameWithBracket() {
        return format("[%s]", name);
    }
}