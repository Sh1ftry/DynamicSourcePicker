package repository;

import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import org.bson.Document;
import rx.Observable;
import source.SensorReading;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

public class ReadingsRepository {

    private final MongoClient client;
    private final MongoDatabase database;

    public ReadingsRepository(final Duration serverSelectionTimeout, final Duration connectionTimeout, final String host) {

        final ClusterSettings clusterSettings = ClusterSettings.builder()
                .serverSelectionTimeout(serverSelectionTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .hosts(Arrays.asList(new ServerAddress(host)))
                .build();

        final SocketSettings socketSettings = SocketSettings.builder()
                .connectTimeout(Math.toIntExact(connectionTimeout.toMillis()), TimeUnit.MILLISECONDS)
                .build();

        final MongoClientSettings clientSettings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings)
                .socketSettings(socketSettings)
                .build();

        client = MongoClients.create(clientSettings);
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
                .onErrorResumeNext(Observable.just(reading));
    }

    public void close() {
        client.close();
    }
}
