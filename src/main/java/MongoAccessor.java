import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import org.bson.Document;
import rx.Observable;

import java.time.Instant;

import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Sorts.ascending;

public class MongoAccessor {

    private final MongoClient client;
    private final MongoDatabase database;

    public MongoAccessor() {
        client = MongoClients.create();
        database = client.getDatabase("readings");
    }

    public Observable<SensorReading> getReadings(Instant timestamp) {
        return database.getCollection("readings")
                .find(gt("timestamp", timestamp.toEpochMilli()))
                .sort(ascending("timestamp")).toObservable()
                .map(document -> new SensorReading(Instant.ofEpochMilli(document.get("timestamp", Long.class)),
                        document.get("reading", Double.class), "database"));
    }

    public Observable<SensorReading> insert(SensorReading reading) {
        return database.getCollection("readings")
                .insertOne(new Document("timestamp",reading.getTimestamp().toEpochMilli())
                        .append("reading", reading.getReading()))
                .map(ignore -> reading)
                .onErrorResumeNext(Observable.just(reading));
    }

    public void close() {
        client.close();
    }
}
