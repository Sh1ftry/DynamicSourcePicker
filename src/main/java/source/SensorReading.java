package source;

import java.time.Instant;
import java.util.Objects;

public class SensorReading implements Comparable<SensorReading> {

    public final Instant timestamp;
    public final Double reading;
    private final String source;

    public SensorReading(Instant timestamp, Double reading, String source) {
        this.timestamp = timestamp;
        this.reading = reading;
        this.source = source;
    }

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

    @Override
    public String toString() {
        return "SensorReading(" + timestamp + ", " + reading + ", " + source + ")";
    }
}
