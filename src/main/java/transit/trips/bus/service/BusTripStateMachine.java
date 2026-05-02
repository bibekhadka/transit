package transit.trips.bus.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import transit.trips.bus.model.BusTrip;
import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;

public class BusTripStateMachine implements TripStateMachine {

	@Override
	public List<Trip> apply(Tap tap, Optional<Trip> activeTrip, FarePolicy farePolicy) {

		return switch (tap.getType()) {
		case ON -> onTap(tap, activeTrip, farePolicy);
		case OFF -> offTap(tap, activeTrip, farePolicy);
		};
	}

	private List<Trip> onTap(Tap tap, Optional<Trip> activeTrip, FarePolicy farePolicy) {
		var trips = new ArrayList<Trip>();
		if (activeTrip.isPresent()) {
			var incomplete = activeTrip.get();
			incomplete.setStatus(TripStatus.INCOMPLETE);
			incomplete.processFare(farePolicy);
			trips.add(incomplete);
		}
		var active = createActiveTrip(tap);
		active.processFare(farePolicy);
		trips.add(active);
		return trips;

	}

	private List<Trip> offTap(Tap tap, Optional<Trip> activeTrip, FarePolicy farePolicy) {
		var trips = new ArrayList<Trip>();
		if (activeTrip.isEmpty()) {
			var incomplete = createIncompleteTrip(tap);
			incomplete.processFare(farePolicy);
			trips.add(incomplete);
			return trips;
		}
		var completedTrip = completeTrip(activeTrip.get(), tap);
		completedTrip.processFare(farePolicy);
		trips.add(completedTrip);
		return trips;
	}

	private BusTrip createActiveTrip(Tap tap) {
		return new BusTrip(tap.getDateTime(), tap.getStopId(), tap.getVehicleId(), tap.getPrimaryAccountNumber(),
				tap.getCompanyId(), TripStatus.ACTIVE);
	}
	
	private BusTrip createIncompleteTrip(Tap tap) {
		return new BusTrip(tap.getDateTime(), tap.getStopId(), tap.getVehicleId(), tap.getPrimaryAccountNumber(),
				tap.getCompanyId(), TripStatus.INCOMPLETE);
	}

	private BusTrip completeTrip(Trip trip, Tap tap) {
		var busTrip = new BusTrip(trip.getId(), trip.getStartedDateTime(), trip.getFromStopId(), trip.getVehicleId(),
				trip.getPrimaryAccountNumber(), trip.getCompanyId(),
				trip.getFromStopId().equals(tap.getStopId()) ? TripStatus.CANCELLED : TripStatus.COMPLETED);
		busTrip.setFinishedDateTime(tap.getDateTime());
		busTrip.setToStopId(tap.getStopId());
		busTrip.setDuration(Duration.between(trip.getStartedDateTime(), tap.getDateTime()));
		return busTrip;

	}

}
