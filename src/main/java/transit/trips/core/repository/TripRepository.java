package transit.trips.core.repository;

import transit.trips.core.model.Trip;

public interface TripRepository {

	void save(Trip trip);

	void remove(Trip trip);

	Trip findById(String id);

}
