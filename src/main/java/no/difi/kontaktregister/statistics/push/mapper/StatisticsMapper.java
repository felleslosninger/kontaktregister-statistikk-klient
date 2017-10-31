package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterValue;
import no.difi.kontaktregister.statistics.util.NameTranslateDefinitions;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static no.difi.kontaktregister.statistics.util.NameTranslateDefinitions.*;

public class StatisticsMapper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<TimeSeriesPoint> map(List<KontaktregisterField> fields, ZonedDateTime fromDateTime) {
        validateFields(fields);

        return mapMeasurements(fields, fromDateTime);
    }

    private List<TimeSeriesPoint> mapMeasurements(List<KontaktregisterField> fields, ZonedDateTime dateTime) {
        Map<NameTranslateDefinitions, List<Long>> measurements = toHashMap(fields);
        return mapBulk(measurements, dateTime);
    }

    private List<TimeSeriesPoint> mapBulk(Map<NameTranslateDefinitions, List<Long>> measurements, ZonedDateTime dateTime) {
        List<TimeSeriesPoint> tsp = new ArrayList<>();
        validateMeasurements(measurements);
        for (int i = 0; i < measurements.get(D5_1).size(); i++) {
            tsp.add(
                    TimeSeriesPoint.builder()
                            .timestamp(dateTime.minusHours(1).plusHours(i))
                            .measurements(getMeasuermentForIndex(measurements, i))
                            .build()
            );
        }
        return tsp;
    }

    private List<Measurement> getMeasuermentForIndex(Map<NameTranslateDefinitions, List<Long>> measurementList, int index) {
        List<Measurement> measurements = Stream.of(D5_1, D5_2, D5_7, D7_4, D7_5, D7_6)
                .map(f -> measurement(measurementList, f, index))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        long valD5i5i6 = measurementList.get(D5_5).get(index) + measurementList.get(D5_6).get(index);
        measurements.add(new Measurement(D5_5_6.getStatisticId(), valD5i5i6));
        return measurements;
    }

    private Optional<Measurement> measurement(Map<NameTranslateDefinitions, List<Long>> measurementList, NameTranslateDefinitions field, int index) {
        return measurementValue(measurementList, index, field).map(v -> new Measurement(field.getStatisticId(), v));
    }

    private Optional<Long> measurementValue(Map<NameTranslateDefinitions, List<Long>> measurementList, int index, NameTranslateDefinitions field) {
        if (measurementList.containsKey(field))
            return Optional.of(measurementList.get(field).get(index));
        else
            return Optional.empty();
    }

    private Map<NameTranslateDefinitions, List<Long>> toHashMap(List<KontaktregisterField> fields) {
        Map<NameTranslateDefinitions, List<Long>> measurements = new HashMap<>();

        for (KontaktregisterField field : fields) {
            if (findD5Id(field) != null) {
                measurements.put(findD5Id(field), toLongList(field.getValues().subList(1, field.getValues().size())));
            } else if (findD7Id(field) != null) {
                measurements.put(findD7Id(field), toLongList(field.getValues().subList(3, field.getValues().size())));
            }
        }

        return measurements;
    }

    private List<Long> toLongList(List<KontaktregisterValue> values) {
        return values.stream()
                .map(e -> Long.valueOf(e.getValue()))
                .collect(toList());
    }

    private NameTranslateDefinitions findD5Id(KontaktregisterField field) {
        return NameTranslateDefinitions.find(field.getValues().get(0).getValue());
    }

    private NameTranslateDefinitions findD7Id(KontaktregisterField field) {
        StringJoiner fieldId = new StringJoiner("");
        if (field.getValues().size() >= 4) {
            for (int i = 0; i < 3; i++) {
                fieldId.add(field.getValues().get(i).getValue());
            }
        }
        NameTranslateDefinitions result = NameTranslateDefinitions.find(fieldId.toString());
        logger.info("Translated field from D7 report, using compound field id <" + fieldId.toString() + ">. Found: " + result);
        return result;
    }

    private void validateFields(List<KontaktregisterField> fields) {
        if (fields == null) {
            throw new MapperError("Point is missing");
        }
    }

    private void validateMeasurements(Map<NameTranslateDefinitions, List<Long>> measurements) {
        if (measurements == null || measurements.size() == 0) {
            throw new MapperError("No valid data after index mapping");
        }
        validateMeasurement(D5_1, measurements);
        validateMeasurement(D5_2, measurements);
        validateMeasurement(D5_5, measurements);
        validateMeasurement(D5_6, measurements);
        validateMeasurement(D5_7, measurements);
//        validateMeasurement(D7_4, measurements);
//        validateMeasurement(D7_5, measurements);
//        validateMeasurement(D7_6, measurements);
    }

    private void validateMeasurement(NameTranslateDefinitions measurement, Map<NameTranslateDefinitions, List<Long>> measurements) {
        if (!measurements.containsKey(measurement))
            throw new MapperError("Measurement " + measurement + " is missing. What we have: " + measurements.keySet());
    }

}
