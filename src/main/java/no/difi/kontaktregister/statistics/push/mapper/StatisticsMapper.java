package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.util.NameTranslateDefinitions;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.String.format;

public class StatisticsMapper {
    public TimeSeriesPoint map(List<KontaktregisterField> fields, ZonedDateTime time) {
        validate(fields);
        List<Measurement> measurements = mapMeasurements(fields);

        return TimeSeriesPoint.builder()
                .timestamp(time)
                .measurements(measurements)
                .build();
    }

    private List<Measurement> mapMeasurements(List<KontaktregisterField> fields) {
        List<Measurement> measurements = new ArrayList<>();
        Long p5p6 = 0L;
        boolean joinedIdForRegisteredUsersWithCellOrEmail = false;

        for (KontaktregisterField field : fields) {
            final String id = joinFieldsForId(field);

            final NameTranslateDefinitions ro = NameTranslateDefinitions.find(id);
            if (ro != null) {
                if (isPartIdOfRegisteredUsersWithCellOrEmail(ro)) {
                    joinedIdForRegisteredUsersWithCellOrEmail = true;
                    p5p6 = p5p6 + new Long(field.getValues().get(field.getValues().size() - 1).getValue());
                }
                else {
                    measurements.add(new Measurement(
                            ro.getStatisticId(),
                            new Long(field.getValues().get(field.getValues().size() - 1).getValue())));
                }
            }
        }
        if (joinedIdForRegisteredUsersWithCellOrEmail) {
            measurements.add(new Measurement(NameTranslateDefinitions.D5_5_6.getStatisticId(), p5p6));
        }
        return measurements;
    }

    private boolean isPartIdOfRegisteredUsersWithCellOrEmail(NameTranslateDefinitions ro) {
        return ro == NameTranslateDefinitions.D5_5 || ro == NameTranslateDefinitions.D5_6;
    }

    private String joinFieldsForId(KontaktregisterField field) {
        StringJoiner fieldId = new StringJoiner("");
        for (int i = 0; i < field.getValues().size() - 1; i++) {
            fieldId.add(field.getValues().get(i).getValue());
        }
        return fieldId.toString();
    }

    private void validate(List<KontaktregisterField> fields) {
        if (fields == null) {
            throw new MapperError("Point is missing");
        }
    }
}
