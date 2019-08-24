import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final ReadingsRepository readingsRepository = new ReadingsRepository();
        final ReadingsSource readingsSource = new ReadingsSource(Duration.ofSeconds(2));
        final ReadingsCache readingsCache = new ReadingsCache(Duration.ofSeconds(15));
        final ReadingsService readingsService = new ReadingsService(readingsRepository, readingsCache, readingsSource);

        boolean running = true;
        Scanner input = new Scanner(System.in);
        while(running) {
            try {
                Duration duration = Duration.parse(input.next());
                Instant to = Instant.now();
                Instant from = to.minus(duration);
                readingsService.getStreamOfReadings(from, to).subscribe(System.out::println);
            } catch (Exception e) {
                running = false;
            }
        }
    }
}
