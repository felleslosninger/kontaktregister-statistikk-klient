package no.difi.kontaktregister.statistics.testutils;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.util.NameTranslateDefinitions;

import static no.difi.kontaktregister.statistics.testutils.KontaktregisterValueObjectMother.createKontaktregisterValue;

public class KontaktregisterFieldObjectMother {
    public static KontaktregisterField createaValidKontaktregisterField() {
        return createKontaktregisterField(NameTranslateDefinitions.D7_4.getKrrField(), "88");
    }

    public static KontaktregisterField createKontaktregisterField(String... values) {
        KontaktregisterField fields = createEmptyKontaktregisterField();
        for (String value : values) {
            fields.getValues().add(createKontaktregisterValue(value));
        }
        return fields;
    }

    public static KontaktregisterField createEmptyKontaktregisterField() {
        return new KontaktregisterField();
    }
}
