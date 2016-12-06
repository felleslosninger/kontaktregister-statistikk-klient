package no.difi.kontaktregister.statistics.push.mapper;

import static java.lang.String.*;

enum ReportD7 {
    D7_1("Inaktive postbokser958935420"),
    D7_2("Inaktive postbokserDigipost984661185"),
    D7_3("Inaktive postboksereBoks996460320"),
    D7_4("Aktive postbokser958935420"),
    D7_5("Aktive postbokserDigipost984661185"),
    D7_6("Aktive postboksereBoks996460320"),
    D7_7("Sum:");

    private final String mapval;

    ReportD7(String mapval) {
        this.mapval = mapval;
    }

    public String mapVal() {
        return mapval;
    }

    public String id() {
        return this.name().toLowerCase();
    }

    public static ReportD7 fromString(String str) {
        if (str != null) {
            for (ReportD7 val : ReportD7.values()) {
                if (str.equalsIgnoreCase(val.mapval)) {
                    return val;
                }
            }
        }
        throw new ReportEnumNotFound(format("Id map from %s not found", str));
    }
}