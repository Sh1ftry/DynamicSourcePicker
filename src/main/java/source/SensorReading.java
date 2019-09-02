package source;

import lombok.Data;
import java.time.Instant;
import java.util.Objects;

@Data
public class SensorReading implements Comparable<SensorReading> {

    private final Instant timestamp;
    private final Double reading;
    private final String source;

    @Override
    public int compareTo(SensorReading o) {
        return timestamp.compareTo(o.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorReading that = (SensorReading) o;
        return timestamp.equals(that.timestamp) &&
                reading.equals(that.reading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, reading);
    }
}
