import rx.Observable;
import rx.subjects.PublishSubject;

import java.sql.Date;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ReadingsSource {

    private final PublishSubject<SensorReading> recent = PublishSubject.create();

    public ReadingsSource() {
        Observable.interval(500, TimeUnit.MILLISECONDS)
                .map(i -> new SensorReading(Date.from(Instant.now()).toInstant(), i.doubleValue(), "stream"))
                .subscribe(recent::onNext);
    }

    public Observable<SensorReading> getStreamOfReadings() {
        return recent;
    }

}
