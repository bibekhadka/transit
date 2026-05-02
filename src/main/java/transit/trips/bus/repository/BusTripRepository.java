package transit.trips.bus.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.repository.TripRepository;

public class BusTripRepository implements TripRepository {

	private final Map<UUID, Trip> trips = new ConcurrentHashMap<>();
	private final Map<String, UUID> activeTripByPan = new ConcurrentHashMap<>();

	@Override
	public void save(Trip trip) {

		trips.put(trip.getId(), trip);

		String pan = trip.getPrimaryAccountNumber();

		if (trip.getStatus() == TripStatus.ACTIVE) {
			activeTripByPan.put(pan, trip.getId());
		} else {
			activeTripByPan.computeIfPresent(pan,
					(key, existingId) -> existingId.equals(trip.getId()) ? null : existingId);
		}
	}

	@Override
	public Optional<Trip> findActiveTripByPrimaryAccountNumber(String pan) {
		if (pan == null) {
			return Optional.empty();
		}
		UUID tripId = activeTripByPan.get(pan);
		return tripId == null ? Optional.empty() : Optional.ofNullable(trips.get(tripId));
	}

	@Override
	public Optional<Trip> findById(UUID id) {
		return Optional.ofNullable(trips.get(id));
	}

	@Override
	public void remove(Trip trip) {
		var removed = trips.remove(trip.getId());
		if (removed != null) {
			activeTripByPan.computeIfPresent(removed.getPrimaryAccountNumber(),
					(pan, existingId) -> existingId.equals(trip.getId()) ? null : existingId);
		}
	}

	@Override
	public List<Trip> findAll() {
		return new ArrayList<>(trips.values());
	}
}