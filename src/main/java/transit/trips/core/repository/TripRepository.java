package transit.trips.core.repository;

import java.util.Optional;

import transit.trips.core.model.Trip;

public interface TripRepository {

	void save(Trip trip);

	void remove(Trip trip);

	Optional<Trip> findById(String id);

}
