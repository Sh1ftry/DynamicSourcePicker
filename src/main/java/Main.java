import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        MongoAccessor mongoAccessor = new MongoAccessor();
        ReadingsSource readingsSource = new ReadingsSource();
        HistoryTable historyTable = new HistoryTable(new TreeSet<>());

        Subscription subscribe = readingsSource.getStreamOfReadings()
                .flatMap(historyTable::insert)
                .flatMap(mongoAccessor::insert)
                .delay(1, TimeUnit.SECONDS)
                .flatMap(historyTable::remove)
                .subscribeOn(Schedulers.io())
                .subscribe();

        Instant from = Instant.now();
        Instant to = from.plus(10, ChronoUnit.SECONDS);
        Observable.just(1).delay(5, TimeUnit.SECONDS)
                .flatMap(ignore -> mongoAccessor.getReadings(from))
                .concatWith(historyTable.getDataSince(from))
                .distinct()
                .concatWith(readingsSource.getStreamOfReadings()
                        .takeWhile(reading -> reading.getTimestamp().isBefore(to)))
                .toBlocking().subscribe(System.out::println);

        subscribe.unsubscribe();
    }
}
