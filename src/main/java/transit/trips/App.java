package transit.trips;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transit.trips.bus.repository.BusTripRepository;
import transit.trips.bus.service.BusTripService;
import transit.trips.bus.service.BusTripStateMachine;
import transit.trips.bus.service.fare.BusFarePolicy;
import transit.trips.core.batch.csv.TripCsvProcessor;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

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
            var repository = new BusTripRepository();
            var fare = new BusFarePolicy();
            var machine = new BusTripStateMachine();
            var tripService = new BusTripService(machine, repository, fare);
            var processor = new TripCsvProcessor(tripService, repository);

            processor.process(input, output);

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            log.info("Trip Processing SUCCESS");
            log.info("Total execution time: {} ms", durationMs);

            System.exit(0);

        } catch (IllegalArgumentException e) {

            log.error("Invalid input provided", e);
            System.exit(2);

        } catch (Exception e) {

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            log.error("Trip Processing FAILED after {} ms", durationMs, e);
            System.exit(1);
        }
    }
}