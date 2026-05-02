package transit.trips.core.batch.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import transit.trips.bus.model.CreditCardTap;
import transit.trips.core.batch.TripBatchProcessor;
import transit.trips.core.model.Tap;
import transit.trips.core.model.TapType;
import transit.trips.core.model.Trip;
import transit.trips.core.repository.TripRepository;
import transit.trips.core.service.TripService;

public class TripCsvProcessor implements TripBatchProcessor {
	private final TripService tripService;
	private final TripRepository repository;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	public TripCsvProcessor(TripService tripService, TripRepository repository) {
		this.tripService = tripService;
		this.repository = repository;
	}

	public void process(String inputPath, String outputPath) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputPath))) {

			reader.readLine(); // Header

			String line;
			while ((line = reader.readLine()) != null) {

				Tap tap = parseTap(line);
				tripService.processTap(tap);
			}
		}

		writeTrips(outputPath);
	}

	private Tap parseTap(String line) {

		String[] parts = line.split(",");

		return new CreditCardTap(TapType.valueOf(parts[2].trim().toUpperCase()), // TapType
				parts[3].trim(), // StopId
				parts[4].trim(), // CompanyId
				parts[5].trim(), // VehicleID
				parts[6].trim(), // PAN
				ZonedDateTime.parse(parts[1].trim(), FORMATTER)); // DateTime
	}

	private void writeTrips(String outputPath) throws IOException {

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath))) {

			writer.write("Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status"); // Header
			writer.newLine();

			List<Trip> trips = repository.findAll();

			for (Trip trip : trips) {
				writer.write(formatTrip(trip));
				writer.newLine();
			}
		}
	}

	private String formatTrip(Trip trip) {
		return String.format("%s, %s, %d, %s, %s, $%.2f, %s, %s, %s, %s", trip.getStartedDateTime().format(FORMATTER),
				trip.getFinishedDateTime() == null ? "" : trip.getFinishedDateTime().format(FORMATTER),
				trip.getDuration() == null ? 0 : trip.getDuration().toSeconds(), trip.getFromStopId(),
				trip.getToStopId(), trip.getChargeAmount(), trip.getCompanyId(), trip.getVehicleId(),
				trip.getPrimaryAccountNumber(), trip.getStatus());
	}

}
