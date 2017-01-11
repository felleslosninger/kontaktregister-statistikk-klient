package no.difi.kontaktregister.statistics.push.mapper;

import no.difi.kontaktregister.statistics.fetch.consumer.KontaktregisterField;
import no.difi.kontaktregister.statistics.util.NameTranslateDefinitions;
import no.difi.statistics.ingest.client.model.Measurement;
import no.difi.statistics.ingest.client.model.TimeSeriesPoint;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static no.difi.kontaktregister.statistics.util.NameTranslateDefinitions.*;

public class StatisticsMapper {
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
        if (validateMeasurements(measurements)) {
            for (int i = 0; i < measurements.get(D5_1).size(); i++) {
                tsp.add(
                        TimeSeriesPoint.builder()
                                .timestamp(dateTime.minusHours(1).plusHours(i))
                                .measurements(getMeasuermentForIndex(measurements, i))
                                .build()
                );
            }
        }
        return tsp;
    }

    private List<Measurement> getMeasuermentForIndex(Map<NameTranslateDefinitions, List<Long>> measurementList, int index) {
        List<Measurement> measurements = new ArrayList<>();
        long valD5i5i6 = measurementList.get(D5_5).get(index) + measurementList.get(D5_6).get(index);
        measurements.add(new Measurement(D5_1.getStatisticId(), measurementList.get(D5_1).get(index)));
        measurements.add(new Measurement(D5_2.getStatisticId(), measurementList.get(D5_2).get(index)));
        measurements.add(new Measurement(D5_5_6.getStatisticId(), valD5i5i6));
        measurements.add(new Measurement(D5_7.getStatisticId(), measurementList.get(D5_7).get(index)));
        measurements.add(new Measurement(D7_4.getStatisticId(), measurementList.get(D7_4).get(index)));
        measurements.add(new Measurement(D7_5.getStatisticId(), measurementList.get(D7_5).get(index)));
        measurements.add(new Measurement(D7_6.getStatisticId(), measurementList.get(D7_6).get(index)));
        return measurements;
    }

    private Map<NameTranslateDefinitions, List<Long>> toHashMap(List<KontaktregisterField> fields) {
        Map<NameTranslateDefinitions, List<Long>> measurements = new HashMap<>();

        for (KontaktregisterField field : fields) {
            final NameTranslateDefinitions ro = findId(field);
            if (ro != null) {
                field.getValues().remove(0);
                measurements.put(ro, toLongList(field));
            }
        }

        return measurements;
    }

    private List<Long> toLongList(KontaktregisterField field) {
        return field.getValues().stream()
                .map(e -> Long.valueOf(e.getValue()))
                .collect(toList());
    }

    private NameTranslateDefinitions findId(KontaktregisterField field) {
        return findD5Id(field) != null ? findD5Id(field) : findD7Id(field);
    }

    private NameTranslateDefinitions findD5Id(KontaktregisterField field) {
        return NameTranslateDefinitions.find(field.getValues().get(0).getValue());
    }

    private NameTranslateDefinitions findD7Id(KontaktregisterField field) {
        StringJoiner fieldId = new StringJoiner("");
        if (field.getValues().size() >= 4) {
            for (int i = 0; i < 4; i++) {
                fieldId.add(field.getValues().get(i).getValue());
            }
        }
        return NameTranslateDefinitions.find(fieldId.toString());
    }

    private void validateFields(List<KontaktregisterField> fields) {
        if (fields == null) {
            throw new MapperError("Point is missing");
        }
    }

    private boolean validateMeasurements(Map<NameTranslateDefinitions, List<Long>> measurements) {
        if (measurements == null || measurements.size() == 0) {
            throw new MapperError("No valid data after index mapping");
        }
        if (!(measurements.containsKey(D5_1)
                && measurements.containsKey(D5_2)
                && measurements.containsKey(D5_5)
                && measurements.containsKey(D5_6)
                && measurements.containsKey(D5_7)
                && measurements.containsKey(D7_4)
                && measurements.containsKey(D5_5)
                && measurements.containsKey(D5_6))
                ) {
            throw new MapperError("Can not map. One or more indexes is missing");
        }
        return true;
    }
}
