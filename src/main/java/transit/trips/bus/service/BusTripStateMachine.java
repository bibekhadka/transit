package transit.trips.bus.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import transit.trips.bus.model.BusTrip;
import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;

@Component
public class BusTripStateMachine implements TripStateMachine {

	private static final Logger log = LoggerFactory.getLogger(BusTripStateMachine.class);

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
			log.info("Marking trip {} as INCOMPLETE due to new ON tap", incomplete.getId());
			incomplete.setStatus(TripStatus.INCOMPLETE);
			incomplete.processFare(farePolicy);
			trips.add(incomplete);
		}
		var active = createActiveTrip(tap);
		log.info("Creating new ACTIVE trip {} from stop {}", active.getId(), tap.getStopId());
		active.processFare(farePolicy);
		trips.add(active);
		return trips;

	}

	private List<Trip> offTap(Tap tap, Optional<Trip> activeTrip, FarePolicy farePolicy) {
		var trips = new ArrayList<Trip>();
		if (activeTrip.isEmpty()) {
			log.info("OFF tap without matching ON. Creating INCOMPLETE trip at stop {}", tap.getStopId());
			var incomplete = createIncompleteTrip(tap);
			incomplete.processFare(farePolicy);
			trips.add(incomplete);
			return trips;
		}
		var completedTrip = completeTrip(activeTrip.get(), tap);
		log.info("Completing trip {} with status {}", completedTrip.getId(), completedTrip.getStatus());
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
