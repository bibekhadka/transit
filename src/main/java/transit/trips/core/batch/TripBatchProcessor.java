package transit.trips.core.batch;

import java.io.IOException;

public interface TripBatchProcessor {
	void process(String inputPath, String outputPath) throws IOException;
}
