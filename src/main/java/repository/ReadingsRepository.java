package repository;

import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import org.bson.Document;
import rx.Observable;
import source.SensorReading;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

public class ReadingsRepository {

    private final MongoClient client;
    private final MongoDatabase database;

    public ReadingsRepository() {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        client = MongoClients.create();
        database = client.getDatabase("readings");
    }

    public Observable<SensorReading> get(final Instant from, final Instant to) {
        return database.getCollection("readings")
                .find(and(gt("timestamp", from.toEpochMilli()), lt("timestamp", to.toEpochMilli())))
                .sort(ascending("timestamp")).toObservable()
                .map(document -> new SensorReading(Instant.ofEpochMilli(document.getLong("timestamp")),
                        document.getDouble("reading"), "database"));
    }

    public Observable<SensorReading> insert(final SensorReading reading) {
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
