package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterFields;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class StatisticsMapper {
    public TimeSeriesPoint mapD5(List<KontaktregisterFields> fields, ZonedDateTime time) {
        validate(fields);

        List<Measurement> measurements = new ArrayList<>();
        for (KontaktregisterFields field : fields) {
            try {
                measurements.add(new Measurement(
                        ReportD5.fromString(field.getValues().get(0).getValue()).name(),
                        new Long(field.getValues().get(1).getValue())));
            } catch (ReportEnumNotFound r) {
                throw new MapperError(format("Failed mapping on %s", field.getValues()), r);
            } catch (NumberFormatException n) {
                throw new MapperError("Value is not a number ", n);
            }
        }
        return TimeSeriesPoint.builder()
                .timestamp(time)
                .measurements(measurements)
                .build();
    }

    public TimeSeriesPoint mapD7(List<KontaktregisterFields> fields, ZonedDateTime time) {
        validate(fields);

        List<Measurement> measurements = new ArrayList<>();
        for (KontaktregisterFields field : fields) {
            try {
                ReportD7 id;
                Long value;
                if (!field.getValues().get(0).getValue().equals(ReportD7.D7_7.mapVal())) {
                    id = ReportD7.fromString(
                            field.getValues().get(0).getValue() +
                                    field.getValues().get(1).getValue() +
                                    field.getValues().get(2).getValue()
                    );
                    value = new Long(field.getValues().get(3).getValue());
                } else {
                    id = ReportD7.fromString(field.getValues().get(0).getValue());
                    value = new Long(field.getValues().get(1).getValue());
                }

                measurements.add(new Measurement(id.name(), value));
            } catch (ReportEnumNotFound r) {
                throw new MapperError(format("Failed mapping on %s", field.getValues()), r);
            } catch (NumberFormatException n) {
                throw new MapperError("Value is not a number ", n);
            }
        }
        return TimeSeriesPoint.builder()
                .timestamp(time)
                .measurements(measurements)
                .build();
    }

    private void validate(List<KontaktregisterFields> fields) {
        if (fields == null) {
            throw new MapperError("Point is missing");
        }
    }
}
