import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        final MongoAccessor mongoAccessor = new MongoAccessor();
        final ReadingsSource readingsSource = new ReadingsSource();
        final HistoryTable historyTable = new HistoryTable();

        final Subscription subscribe = readingsSource.getStreamOfReadings()
                .flatMap(historyTable::insert)
                .flatMap(mongoAccessor::insert)
                .delay(2000, TimeUnit.MILLISECONDS)
                .flatMap(historyTable::remove)
                .subscribeOn(Schedulers.io())
                .subscribe();

        final Instant from = Instant.now().plus(5000, ChronoUnit.MILLIS);
        final Instant to = from.plus(1000, ChronoUnit.MILLIS);
        Observable.timer(5200, TimeUnit.MILLISECONDS)
                .flatMap(ignore -> mongoAccessor.getReadings(from, to))
                .concatWith(historyTable.getReadings(from, to))
                .distinct()
                .concatWith(readingsSource.getStreamOfReadings(to))
                .toBlocking().subscribe(System.out::println);

        subscribe.unsubscribe();
        mongoAccessor.close();
    }
}
