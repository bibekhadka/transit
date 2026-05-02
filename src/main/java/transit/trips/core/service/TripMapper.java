package transit.trips.core.service;

import java.util.List;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;

public interface TripMapper {

	List<Trip> toTrip(Tap tap);

}
