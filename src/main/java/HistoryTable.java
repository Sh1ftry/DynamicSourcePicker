import rx.Observable;

import java.time.Instant;
import java.util.Collections;
import java.util.SortedSet;

public class HistoryTable {
    private final SortedSet<SensorReading> recentData;

    public HistoryTable(final SortedSet<SensorReading> collection) {
        this.recentData = Collections.synchronizedSortedSet(collection);
    }

    public Observable<SensorReading> getDataSince(final Instant timestamp) {
        return Observable.from(recentData)
                .filter(r -> r.getTimestamp().isAfter(timestamp))
                .map(r -> new SensorReading(r.getTimestamp(), r.getReading(), "history"));
    }

    public Observable<SensorReading> insert(final SensorReading sensorReading) {
        return Observable.fromCallable(() -> {
            recentData.add(sensorReading);
            return sensorReading;
        });
    }

    public Observable<SensorReading> remove(final SensorReading sensorReading) {
        return Observable.fromCallable(() -> {
            recentData.removeIf(reading -> reading.compareTo(sensorReading) <= 0);
            return sensorReading;
        });
    }
}
