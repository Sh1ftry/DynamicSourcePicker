import io.reactivex.Observable;
import io.reactivex.Observer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HistoryTable {
    private List<SensorReading> recentData;

    public HistoryTable(Observable<SensorReading> recentReadings,
                        Observer<List<SensorReading>> persistenceStream,
                        Observable<String> persistenceNotifications) {
        this.recentData = new ArrayList<>();
        recentReadings.subscribe(r -> {
            recentData.add(r);
            if(recentData.size() > 8) persistenceStream.onNext(recentData);
        });
        persistenceNotifications.subscribe(n -> recentData.clear());
    }

    public Observable<SensorReading> getDataSince(Instant timestamp) {
        return Observable.fromIterable(recentData)
                .filter(r -> r.getTimestamp().isAfter(timestamp)).doOnNext(r -> System.out.println("From HT"));
    }
}
