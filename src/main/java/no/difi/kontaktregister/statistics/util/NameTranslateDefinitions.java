package no.difi.kontaktregister.statistics.util;

import java.util.Arrays;

public enum NameTranslateDefinitions {
    D5_1("Aktive brukere med e-post", "aktiveBrukereMedEpost"),
    D5_2("Aktive brukere med mobil", "aktiveBrukereMedMobil"),
    D5_5("Aktive brukere med reservasjon og e-post eller mobil", "aktiveBrukereMedReservasjonOgEpostEllerMobil"),
    D5_6("Aktive brukere med reservasjon uten verken e-post eller mobil", "aktiveBrukereMedReservasjonUtenEpostEllerMobil"),
    D5_5_6("Antall reserverte personer med e-post eller mobil", "antallReservertePersonerMedEpostEllerMobil"),
    D5_7("Antall aktive brukere i kontaktregisteret", "antallAktiveBrukereIKontaktregisteret"),
    D7_4("Aktive postbokser958935420", "antallPersonerMedAktivDigitalPostkasse"),
    D7_5("Aktive postbokserDigipost984661185", "antallPersonerMedDigipost"),
    D7_6("Aktive postboksereBoks996460320", "antallPersonerMedEboks");

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
