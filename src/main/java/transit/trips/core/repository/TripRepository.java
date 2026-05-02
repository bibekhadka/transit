package transit.trips.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import transit.trips.core.model.Trip;

public interface TripRepository {

	void save(Trip trip);

	void remove(Trip trip);

	Optional<Trip> findById(UUID id);

	Optional<Trip> findActiveTripByPrimaryAccountNumber(String pan);

	List<Trip> findAll();

}
