import cache.ReadingsCache;
import repository.ReadingsRepository;
import service.ReadingsService;
import source.ReadingsSource;
import view.UserInterface;

import java.time.Duration;

public class App {

    private static final Duration READINGS_FREQUENCY = Duration.ofSeconds(2);
    private static final Duration MINIMUM_CACHE_TIME = Duration.ofSeconds(15);
    private static final Duration DB_SERVER_CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration DB_CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private static final String DB_HOST = "localhost";

    public static void main(String[] args) {
        final ReadingsRepository readingsRepository = new ReadingsRepository(DB_SERVER_CONNECTION_TIMEOUT, DB_CONNECTION_TIMEOUT, DB_HOST);
        final ReadingsSource readingsSource = new ReadingsSource(READINGS_FREQUENCY);
        final ReadingsCache readingsCache = new ReadingsCache(MINIMUM_CACHE_TIME);
        final ReadingsService readingsService = new ReadingsService(readingsRepository, readingsCache, readingsSource);
        final UserInterface ui = new UserInterface(System.in, System.out);

        ui.show(Duration::parse, Duration::parse, readingsService::getStreamOfReadings);
        readingsService.close();
    }
}
