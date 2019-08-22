import rx.Observable;

import java.time.Instant;
import java.util.concurrent.ConcurrentSkipListSet;

public class HistoryTable {
    private final ConcurrentSkipListSet<SensorReading> recentReadings;

    public HistoryTable() {
        this.recentReadings = new ConcurrentSkipListSet<>();
    }

    public Observable<SensorReading> getReadings(final Instant from, final Instant to) {
        return Observable.from(recentReadings)
                .filter(r -> r.getTimestamp().isAfter(from) && r.getTimestamp().isBefore(to))
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
