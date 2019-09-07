package repository;

import com.mongodb.ConnectionString;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import rx.Observable;
import source.SensorReading;

import java.net.URI;
import java.time.Instant;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

public class ReadingsRepository {

    private final MongoClient client;
    private final MongoDatabase database;
    private static final Logger logger = LogManager.getLogger(ReadingsRepository.class);

    public ReadingsRepository(final URI connectionString) {
        client = MongoClients.create(new ConnectionString(connectionString.toString()));
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
                .insertOne(new Document("timestamp",reading.timestamp.toEpochMilli())
                        .append("reading", reading.reading))
                .map(ignore -> reading)
                .doOnError(logger::warn)
                .onErrorResumeNext(Observable.just(reading));
    }

    public void close() {
        client.close();
    }
}
