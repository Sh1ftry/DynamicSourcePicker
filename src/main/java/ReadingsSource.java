import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ReadingsSource {

    private final Subject<SensorReading> recent = PublishSubject.create();

    public ReadingsSource() {
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> new SensorReading(Instant.now(), i.doubleValue()))
                .subscribe(recent::onNext);
    }

    public Observable<SensorReading> getStreamOfReadings() {
        return recent;
    }

}
