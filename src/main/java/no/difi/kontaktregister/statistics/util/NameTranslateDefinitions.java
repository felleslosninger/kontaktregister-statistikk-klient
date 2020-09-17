package no.difi.kontaktregister.statistics.util;

import java.util.Arrays;

public enum NameTranslateDefinitions {
    D5_1("Aktive brukere med e-post", "brukereMedEpost"),
    D5_2("Aktive brukere med mobil", "brukereMedMobil"),
    D5_4("Aktive brukere med e-post og/eller mobil", "brukereMedEpostOgEllerMobil"),
    D5_5("Aktive brukere med reservasjon og e-post eller mobil", "brukereMedReservasjonOgEpostEllerMobil"),
    D5_6("Aktive brukere med reservasjon uten verken e-post eller mobil", "brukereMedReservasjonUtenVerkenEpostEllerMobil"),
    D5_5_6("-", "brukereMedReservasjon"),
    D5_7("Antall aktive brukere i kontaktregisteret", "brukereIKontaktregisteret"),
    D7_3("Aktive postbokserDigipost984661185", "brukereMedDigipost"),
    D7_4_OLD("Aktive postboksereBoks996460320", "brukereMedEboks"),
    D7_4("Aktive postboksereBoks922020175", "brukereMedEboks"),
    D7_3_4("-", "brukereMedPostkasse");

    private final String krrField;
    private final String statisticId;

    NameTranslateDefinitions(String krrField, String statisticId) {
        this.krrField = krrField;
        this.statisticId = statisticId;
    }

    public String getKrrField() {
        return krrField;
    }

    public String getStatisticId() {
        return statisticId;
    }

    public static NameTranslateDefinitions find(String str) {
        return Arrays.stream(NameTranslateDefinitions.values())
                .filter(e -> e.krrField.equals(str))
                .findAny()
                .orElse(null);
    }
}
