import cache.ReadingsCache;
import repository.ReadingsRepository;
import service.ReadingsService;
import source.ReadingsSource;
import view.UserInterface;

import java.net.URI;
import java.time.Duration;

public class App {

    private static final Duration READINGS_FREQUENCY = Duration.ofMillis(500);
    private static final Duration MINIMUM_CACHE_TIME = Duration.ofSeconds(5);
    private static final URI DB_CONNECTION_STRING = URI.create("mongodb://username:QZVCC37sB0PC5Czz@" +
            "cluster0-shard-00-00-6ai61.gcp.mongodb.net:27017," +
            "cluster0-shard-00-01-6ai61.gcp.mongodb.net:27017," +
            "cluster0-shard-00-02-6ai61.gcp.mongodb.net:27017/" +
            "readings?" +
            "streamType=netty&" +
            "ssl=true&" +
            "replicaSet=Cluster0-shard-0&" +
            "authSource=admin&" +
            "w=majority");

    public static void main(String[] args) {
        final ReadingsRepository readingsRepository = new ReadingsRepository(DB_CONNECTION_STRING);
        final ReadingsSource readingsSource = new ReadingsSource(READINGS_FREQUENCY);
        final ReadingsCache readingsCache = new ReadingsCache(MINIMUM_CACHE_TIME);
        final ReadingsService readingsService = new ReadingsService(readingsRepository, readingsCache, readingsSource);
        final UserInterface ui = new UserInterface(System.in, System.out);

        ui.show(Duration::parse, Duration::parse, readingsService::getStreamOfReadings);
        readingsService.close();
    }
}
