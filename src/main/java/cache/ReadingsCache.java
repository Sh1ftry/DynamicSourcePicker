package cache;

import rx.Observable;
import source.SensorReading;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class ReadingsCache {
    private final ConcurrentSkipListSet<SensorReading> recentReadings;
    private final Duration minimalCacheTtl;

    public ReadingsCache(final Duration minimalCacheTtl) {
        this.recentReadings = new ConcurrentSkipListSet<>();
        this.minimalCacheTtl = minimalCacheTtl;
    }

    public Observable<SensorReading> get(final Instant from, final Instant to) {
        return Observable.from(recentReadings)
                .filter(r -> r.timestamp.isAfter(from) && r.timestamp.isBefore(to))
                .map(r -> new SensorReading(r.timestamp, r.reading, "cache"));
    }

    public Observable<SensorReading> insert(final SensorReading sensorReading) {
        return Observable.fromCallable(() -> {
            recentReadings.add(sensorReading);
            return sensorReading;
        });
    }

    public Observable<SensorReading> remove(final SensorReading sensorReading) {
        return Observable.defer(() -> Observable
                .timer(minimalCacheTtl.toMillis(), TimeUnit.MILLISECONDS)
                .map(ignore -> {
                    recentReadings.remove(sensorReading);
                    return sensorReading;
                }));
    }

    public Observable<Instant> oldest() {
        return Observable.fromCallable(() -> recentReadings.first().timestamp);
    }
}
