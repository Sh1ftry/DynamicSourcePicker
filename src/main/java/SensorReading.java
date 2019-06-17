import lombok.Data;
import java.time.Instant;

@Data
public class SensorReading {

    private final Instant timestamp;
    private final Double reading;

}
