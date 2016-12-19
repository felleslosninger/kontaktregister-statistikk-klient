package no.difi.kontaktregister.statistics.testutils;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;

class KontaktregisterValueObjectMother {
    static KontaktregisterValue createKontaktregisterValue(String value) {
        KontaktregisterValue krv = new KontaktregisterValue();
        krv.setValue(value);
        return krv;
    }
}
