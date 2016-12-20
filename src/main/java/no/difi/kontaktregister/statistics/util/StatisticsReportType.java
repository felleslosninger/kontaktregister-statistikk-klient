package no.difi.kontaktregister.statistics.util;

import static java.lang.String.format;

public enum StatisticsReportType {
    kontaktregister("kontaktogreservasjonsregister", "991825827");

    private final String seriesId;
    private final String owner;

    StatisticsReportType(String owner, String seriesId) {
        this.owner = owner;
        this.seriesId = seriesId;
    }

    public String seriesId() {
        return seriesId;
    }

    public String owner() {
        return owner;
    }

    public String getIdWithBracket() {
        return format("[%s]", seriesId);
    }
}
