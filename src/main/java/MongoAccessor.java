import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import org.bson.Document;
import rx.Observable;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

public class MongoAccessor {

    private final MongoClient client;
    private final MongoDatabase database;

    public MongoAccessor() {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        client = MongoClients.create();
        database = client.getDatabase("readings");
    }

    public Observable<SensorReading> getReadings(final Instant from, final Instant to) {
        return database.getCollection("readings")
                .find(and(gt("timestamp", from.toEpochMilli()), lt("timestamp", to.toEpochMilli())))
                .sort(ascending("timestamp")).toObservable()
                .map(document -> new SensorReading(Instant.ofEpochMilli(document.getLong("timestamp")),
                        document.getDouble("reading"), "database"));
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
