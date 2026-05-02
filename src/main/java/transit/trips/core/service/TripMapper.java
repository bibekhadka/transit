package transit.trips.core.service;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;

public interface TripMapper {

	Trip toTrip(Tap tap);

}
