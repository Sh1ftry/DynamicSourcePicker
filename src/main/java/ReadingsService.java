import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.time.Instant;

public class ReadingsService {

    private final ReadingsRepository repository;
    private final ReadingsCache cache;
    private final ReadingsSource source;

    public ReadingsService(final ReadingsRepository repository, final ReadingsCache cache, final ReadingsSource source) {
        this.repository = repository;
        this.cache = cache;
        this.source = source;
        persistStreamOfReadings(source.getStreamOfReadings());
    }

    private Subscription persistStreamOfReadings(final Observable<SensorReading> streamOfReadings) {
        return streamOfReadings
                .flatMap(cache::insert)
                .flatMap(repository::insert)
                .flatMap(cache::remove)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public Observable<SensorReading> getStreamOfReadings(Duration from, int elements) {
        return repository.get(from, to)
                .concatWith(cache.getReadings(from, to))
                .distinct()
                .concatWith(source.getStreamOfReadings(to))
                .take(elements);
    }

}
