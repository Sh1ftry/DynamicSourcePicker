import cache.ReadingsCache;
import repository.ReadingsRepository;
import service.ReadingsService;
import source.ReadingsSource;
import view.UserInterface;

import java.time.Duration;

public class App {

    private static final int READINGS_FREQUENCY = 2;
    private static final int MINIMUM_CACHE_TIME = 15;

    public static void main(String[] args) {
        final ReadingsRepository readingsRepository = new ReadingsRepository();
        final ReadingsSource readingsSource = new ReadingsSource(Duration.ofSeconds(READINGS_FREQUENCY));
        final ReadingsCache readingsCache = new ReadingsCache(Duration.ofSeconds(MINIMUM_CACHE_TIME));
        final ReadingsService readingsService = new ReadingsService(readingsRepository, readingsCache, readingsSource);
        final UserInterface ui = new UserInterface(System.in, System.out);

        ui.show(Duration::parse, Duration::parse, readingsService::getStreamOfReadings);
        readingsService.close();
    }
}
