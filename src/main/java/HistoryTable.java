import rx.Observable;

import java.time.Instant;
import java.util.Collections;
import java.util.SortedSet;

public class HistoryTable {
    private final SortedSet<SensorReading> recentReadings;

    public HistoryTable(final SortedSet<SensorReading> collection) {
        this.recentReadings = Collections.synchronizedSortedSet(collection);
    }

    public Observable<SensorReading> getReadings(final Instant timestamp) {
        return Observable.from(recentReadings)
                .filter(r -> r.getTimestamp().isAfter(timestamp))
                .map(r -> new SensorReading(r.getTimestamp(), r.getReading(), "history"));
    }

    public Observable<SensorReading> insert(final SensorReading sensorReading) {
        return Observable.fromCallable(() -> {
            recentReadings.add(sensorReading);
            return sensorReading;
        });
    }

    public Observable<SensorReading> remove(final SensorReading sensorReading) {
        return Observable.fromCallable(() -> {
            recentReadings.removeIf(reading -> reading.compareTo(sensorReading) <= 0);
            return sensorReading;
        });
    }
}
