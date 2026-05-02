package transit.trips.bus.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import transit.trips.bus.model.BusTrip;
import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.repository.TripRepository;
import transit.trips.core.service.TripMapper;

public class BusTripMapper implements TripMapper {

	private final TripRepository repository;

	public BusTripMapper(TripRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<Trip> toTrip(Tap tap) {
		var existingTrip = repository.findById(tap.getPrimaryAccountNumber());

		return switch (tap.getType()) {
		case ON -> handleTapOn(tap, existingTrip);
		case OFF -> handleTapOff(tap, existingTrip);
		};
	}

	private List<Trip> handleTapOn(Tap tap, Optional<Trip> openTrip) {
		var trips = new ArrayList<Trip>();
		if (openTrip.isPresent()) {
			openTrip.get().setStatus(TripStatus.INCOMPLETE);
			trips.add(openTrip.get());
		}
		trips.add(createNewTrip(tap));
		return trips;

	}

	private List<Trip> handleTapOff(Tap tap, Optional<Trip> openTrip) {
		var trips = new ArrayList<Trip>();
		if (openTrip.isEmpty()) {
			var incompleteTrip = createNewTrip(tap);
			incompleteTrip.setStatus(TripStatus.INCOMPLETE);
			trips.add(incompleteTrip);
			return trips;
		}
		var completedTrip = completeTrip(openTrip.get(), tap);
		trips.add(completedTrip);
		return trips;
	}

	private BusTrip createNewTrip(Tap tap) {
		return new BusTrip(tap.getDateTime(), tap.getStopId(), tap.getVehicleId(), tap.getPrimaryAccountNumber(),
				tap.getCompanyId());

	}

	private BusTrip completeTrip(Trip trip, Tap tap) {
		var busTrip = new BusTrip(trip.getStartedDateTime(), trip.getFromStopId(), trip.getVehicleId(),
				trip.getPrimaryAccountNumber(), trip.getCompanyId());
		busTrip.setFinishedDateTime(tap.getDateTime());
		busTrip.setToStopId(tap.getStopId());
		busTrip.setDuration(Duration.between(trip.getStartedDateTime(), tap.getDateTime()));
		busTrip.setStatus(TripStatus.COMPLETED);
		return busTrip;

	}

}
