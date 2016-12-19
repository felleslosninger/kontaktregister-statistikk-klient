package no.difi.kontaktregister.statistics.util;

import static java.lang.String.format;

public enum StatisticsReportType {
    kontaktregister("kontaktogreservasjonsregister");

    private final String statisticId;

    StatisticsReportType(String statisticId) {
        this.statisticId = statisticId;
    }

    public String getStatisticId() {
        return statisticId;
    }
    public String getIdWithBracket() {
        return format("[%s]", statisticId);
    }
}
