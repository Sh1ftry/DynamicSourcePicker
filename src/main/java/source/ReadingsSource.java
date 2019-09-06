package source;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ReadingsSource {

    private final PublishSubject<SensorReading> recent = PublishSubject.create();
    private final Subscription sourceSubscription;

    public ReadingsSource(Duration interval) {
        this.sourceSubscription = Observable.interval(interval.toMillis(), TimeUnit.MILLISECONDS)
                .map(i -> new SensorReading(Instant.ofEpochMilli(System.currentTimeMillis()), i.doubleValue(), "stream"))
                .subscribeOn(Schedulers.io())
                .subscribe(recent::onNext);
    }

    public Observable<SensorReading> getStreamOfReadings(Instant to) {
        return recent.takeWhile(reading -> reading.timestamp.isBefore(to));
    }

    public Observable<SensorReading> getStreamOfReadings() {
        return recent;
    }

    public void close() {
       sourceSubscription.unsubscribe();
    }
}
