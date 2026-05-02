package transit.trips;

import transit.trips.bus.repository.BusTripRepository;
import transit.trips.bus.service.BusTripService;
import transit.trips.bus.service.BusTripStateMachine;
import transit.trips.bus.service.fare.BusFarePolicy;
import transit.trips.core.batch.csv.TripCsvProcessor;

public class App {

	public static void main(String[] args) throws Exception {

		String input;
		String output;

		if (args.length >= 2) {
			input = args[0];
			output = args[1];
		} else {
			input = "src/main/resources/input/taps.csv";
			output = "output/trips.csv";
		}

		var repository = new BusTripRepository();
		var fare = new BusFarePolicy();
		var machine = new BusTripStateMachine();
		var tripService = new BusTripService(machine, repository, fare);
		var processor = new TripCsvProcessor(tripService, repository);

		System.out.println("Processing input: " + input);
		System.out.println("Writing output: " + output);

		processor.process(input, output);

		System.out.println("Processing complete.");
	}
}