package transit.trips.core.service;

import java.util.List;
import java.util.Optional;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.service.fare.FarePolicy;

public interface TripStateMachine {

	List<Trip> apply(Tap tap, Optional<Trip> activeTrip, FarePolicy farePolicy);

}
