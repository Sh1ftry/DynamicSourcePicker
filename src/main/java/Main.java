import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.time.Instant;
import java.util.List;

public class Main {

    private static final String DB_USERNAME = "username";
    private static final String DB_PASSWORD = "<password>";
    private static final String DB_SERVER = "<server>";
    private static final String DB_NAME = "readings";
    private static final String DB_OPTIONS = "retryWrites=true&w=majority";

    public static void main(String[] args) throws InterruptedException {
        Subject<List<SensorReading>> persistenceSubject = PublishSubject.create();
        Subject<String> notificationsSubject = PublishSubject.create();

        MongoAccessor mongoAccessor = new MongoAccessor(DB_USERNAME, DB_PASSWORD, DB_SERVER, DB_NAME, DB_OPTIONS,
                persistenceSubject, notificationsSubject);

        ReadingsSource readingsSource = new ReadingsSource();

        HistoryTable historyTable = new HistoryTable(readingsSource.getStreamOfReadings(), persistenceSubject,
                notificationsSubject);

        Thread.sleep(10000);
        Instant then = Instant.now();
        Thread.sleep(500);
        mongoAccessor.getDataSince(then)
                .concatWith(historyTable.getDataSince(then))
                .concatWith(readingsSource.getStreamOfReadings())
                .blockingSubscribe(System.out::println);
    }

}
