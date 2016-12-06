package no.difi.kontaktregister.statistics.push.mapper;

import static java.lang.String.format;

public enum ReportD5 {
    D5_1("Aktive brukere med e-post"),
    D5_2("Aktive brukere med mobil"),
    D5_3("Aktive brukere med e-post og mobil"),
    D5_4("Aktive brukere med e-post og/eller mobil"),
    D5_5("Aktive brukere med reservasjon og e-post eller mobil"),
    D5_6("Aktive brukere med reservasjon uten verken e-post eller mobil"),
    D5_7("Antall aktive brukere i kontaktregisteret"),
    D5_8("Antall slettede brukere i kontaktregisteret"),
    D5_9("Toalt antall brukere i kontaktregisteret"),
    D5_10("Antall brukere som har blitt varslet"),
    D5_11("Antall brukere som har utløpt");

    private final String mapval;

    ReportD5(String mapval) {
        this.mapval = mapval;
    }

    public String mapVal() {
        return mapval;
    }

    public String id() {
        return this.name().toLowerCase();
    }

    public static ReportD5 fromString(String str) {
        if (str != null) {
            for (ReportD5 val : ReportD5.values()) {
                if (str.equalsIgnoreCase(val.mapval)) {
                    return val;
                }
            }
        }
        throw new ReportEnumNotFound(format("Id map from %s not found", str));
    }
}
