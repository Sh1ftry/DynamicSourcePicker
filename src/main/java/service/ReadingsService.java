package service;

import cache.ReadingsCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.ReadingsRepository;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import source.ReadingsSource;
import source.SensorReading;

import java.time.Duration;
import java.time.Instant;

public class ReadingsService {

    private final ReadingsRepository repository;
    private final ReadingsCache cache;
    private final ReadingsSource source;
    private final Subscription persistenceSubscription;
    private static final Logger logger = LogManager.getLogger(ReadingsService.class);

    public ReadingsService(final ReadingsRepository repository, final ReadingsCache cache, final ReadingsSource source) {
        this.repository = repository;
        this.cache = cache;
        this.source = source;
        this.persistenceSubscription = persistStreamOfReadings(source.getStreamOfReadings());
    }

    public Observable<SensorReading> getStreamOfReadings(final Duration durationBefore, final Duration durationAfter) {
        Instant now = Instant.now();
        final Instant from = now.minus(durationBefore);
        final Instant to = from.plus(durationAfter);
        return cache.oldest().flatMap(oldestCachedTimestamp -> {
            final Observable<SensorReading> cachedReadings = cache.get(from, to);
            if(oldestCachedTimestamp.isBefore(from)) return cachedReadings;
            else return repository.get(from, oldestCachedTimestamp)
                    .doOnError(e -> logger.warn("Error while getting readings from database"))
                    .onErrorResumeNext(Observable.empty())
                    .concatWith(cachedReadings).distinct();
        }).concatWith(source.getStreamOfReadings(to));
    }

    public void close() {
        persistenceSubscription.unsubscribe();
        source.close();
        repository.close();
    }

    private Subscription persistStreamOfReadings(final Observable<SensorReading> streamOfReadings) {
        return streamOfReadings
                .flatMap(cache::insert)
                .flatMap(repository::insert)
                .flatMap(cache::remove)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

}
