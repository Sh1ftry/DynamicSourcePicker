import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        final MongoAccessor mongoAccessor = new MongoAccessor();
        final ReadingsSource readingsSource = new ReadingsSource();
        final HistoryTable historyTable = new HistoryTable(new TreeSet<>());

        final Subscription subscribe = readingsSource.getStreamOfReadings()
                .flatMap(historyTable::insert)
                .flatMap(mongoAccessor::insert)
                .delay(30, TimeUnit.SECONDS)
                .flatMap(historyTable::remove)
                .subscribeOn(Schedulers.io())
                .subscribe();

        final Instant from = Instant.now();
        final Instant to = from.plus(10, ChronoUnit.SECONDS);
        Observable.timer(5, TimeUnit.SECONDS)
                .flatMap(ignore -> mongoAccessor.getReadings(from))
                .concatWith(historyTable.getReadings(from))
                .distinct()
                .concatWith(readingsSource.getStreamOfReadings()
                        .takeWhile(reading -> reading.getTimestamp().isBefore(to)))
                .toBlocking().subscribe(System.out::println);

        subscribe.unsubscribe();
        mongoAccessor.close();
    }
}
