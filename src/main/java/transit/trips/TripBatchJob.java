package transit.trips;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import transit.trips.core.batch.csv.TripCsvProcessor;

@Component
public class TripBatchJob implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	private final TripCsvProcessor processor;

	public TripBatchJob(TripCsvProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void run(String... args) {
		String input;
		String output;

		if (args.length >= 2) {
			input = args[0];
			output = args[1];
		} else {
			input = "src/main/resources/input/taps.csv";
			output = "output/trips.csv";
		}

		log.info("Starting Trip Processing Job");
		log.info("Input: {}", input);
		log.info("Output: {}", output);

		long startTime = System.nanoTime();

		try {
			processor.process(input, output);

			long durationMs = (System.nanoTime() - startTime) / 1_000_000;
			log.info("Trip Processing SUCCESS");
			log.info("Total execution time: {} ms", durationMs);

		} catch (Exception e) {

			long durationMs = (System.nanoTime() - startTime) / 1_000_000;
			log.error("Trip Processing FAILED after {} ms", durationMs, e);

			System.exit(1);
		}
	}
}