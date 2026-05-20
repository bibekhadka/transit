package transit.trips.bus.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.repository.TripRepository;
import static transit.trips.core.util.MaskingUtil.maskPan;

@Repository
public class BusTripRepository implements TripRepository {

	private static final Logger log = LoggerFactory.getLogger(BusTripRepository.class);

	private final Map<UUID, Trip> trips = new ConcurrentHashMap<>();
	private final Map<String, UUID> activeTripByPan = new ConcurrentHashMap<>();

	@Override
	public void save(Trip trip) {

		Objects.requireNonNull(trip, "Trip must not be null.");

		trips.put(trip.getId(), trip);

		String pan = trip.getPrimaryAccountNumber();

		log.debug("Saving trip id={} pan={} status={}", trip.getId(), maskPan(pan), trip.getStatus());

		if (trip.getStatus() == TripStatus.ACTIVE) {

			activeTripByPan.put(pan, trip.getId());

			log.debug("Marked trip id={} as ACTIVE for pan={}", trip.getId(), maskPan(pan));

		} else {
			activeTripByPan.computeIfPresent(pan,
					(key, existingId) -> existingId.equals(trip.getId()) ? null : existingId);

			log.debug("Cleared ACTIVE trip mapping for pan={} if matching id={}", maskPan(pan), trip.getId());
		}
	}

	@Override
	public Optional<Trip> findActiveTripByPrimaryAccountNumber(String pan) {
		if (pan == null) {
			log.warn("Attempted to find active trip with null PAN");
			return Optional.empty();
		}

		UUID tripId = activeTripByPan.get(pan);
		if (tripId == null) {
			log.debug("No active trip found for pan={}", maskPan(pan));
			return Optional.empty();
		}

		Trip trip = trips.get(tripId);
		if (trip == null) {
			log.warn("Active trip id={} found in index but missing in storage for pan={}", tripId, maskPan(pan));
		}
		return Optional.ofNullable(trip);
	}

	@Override
	public Optional<Trip> findById(UUID id) {
		log.debug("Finding trip by id={}", id);
		return Optional.ofNullable(trips.get(id));
	}

	@Override
	public void remove(Trip trip) {
		if (trip == null) {
			log.warn("Attempted to remove null trip");
			return;
		}
		var removed = trips.remove(trip.getId());
		if (removed != null) {
			activeTripByPan.computeIfPresent(removed.getPrimaryAccountNumber(),
					(pan, existingId) -> existingId.equals(trip.getId()) ? null : existingId);
			log.info("Removed trip id={} pan={}", trip.getId(), maskPan(trip.getPrimaryAccountNumber()));
		} else {
			log.warn("Attempted to remove non-existent trip id={}", trip.getId());
		}
	}

	@Override
	public List<Trip> findAll() {
		log.debug("Fetching all trips. Total={}", trips.size());
		return new ArrayList<>(trips.values());
	}
}