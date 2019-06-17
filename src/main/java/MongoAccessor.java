import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.schedulers.Schedulers;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;

public class MongoAccessor {

    private final MongoCollection<Document> readings;
    private final Observable<List<SensorReading>> persistenceDataStream;
    private final Observer<String> persistenceNotificationsStream;

    public MongoAccessor(String username, String password, String server, String database, String options,
                         Observable<List<SensorReading>> persistenceDataStream, Observer<String> persistenceNotificationsStream) {
        this.persistenceDataStream = persistenceDataStream;
        this.persistenceNotificationsStream = persistenceNotificationsStream;
        MongoClientURI uri = new MongoClientURI("mongodb+srv://" + username + ":" + password + "@" + //
                server + "/" + database + "?" + options);
        readings = new MongoClient(uri).getDatabase(database).getCollection("sensor");
        readings.deleteMany(lt("timestamp", Instant.now()));
        this.persistenceDataStream.subscribeOn(Schedulers.io()).subscribe(this::writeDataAndNotify);
    }

    public Observable<SensorReading> getDataSince(Instant timestamp) {
        return Observable.fromCallable(() -> getStreamOfReadings(timestamp)
                .map(this::convertDocumentToSensorReading)
                .collect(Collectors.toList())
        ).concatMapIterable(sr -> sr).doOnNext(r -> System.out.println("From DB"));
    }

    private Stream<Document> getStreamOfReadings(Instant timestamp) {
        return StreamSupport.stream(readings.find(gt("timestamp", timestamp)).spliterator(), false);
    }

    private void writeDataAndNotify(List<SensorReading> readingsList) {
        readings.bulkWrite(convertListToWriteModelList(readingsList));
        persistenceNotificationsStream.onNext("notification");
    }

    private List<WriteModel<Document>> convertListToWriteModelList(List<SensorReading> readingsList) {
        return readingsList.stream()
                .map(r -> new InsertOneModel<>(convertSensorReadingToDocument(r)))
                .collect(Collectors.toList());
    }

    private SensorReading convertDocumentToSensorReading(Document document) {
        return new SensorReading(document.get("timestamp", Date.class).toInstant(),
                document.get("reading", Double.class));
    }

    private Document convertSensorReadingToDocument(SensorReading reading) {
        return new Document("timestamp", reading.getTimestamp()).append("reading", reading.getReading());
    }
}
