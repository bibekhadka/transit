package transit.trips.bus.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import transit.trips.bus.model.BusTrip;
import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BusTripRepositoryTest {

	private BusTripRepository repository;

	@BeforeEach
	void setUp() {
		repository = new BusTripRepository();
	}

	// --------------------------------------------
	// save ACTIVE trip
	// --------------------------------------------
	@Test
	void whenSaveThenSaveAndIndexActiveTrip() {
		var id = UUID.randomUUID();
		var pan = "5500005555555559";

		var trip = new BusTrip(id, ZonedDateTime.now(), "Stop1", "Bus1", pan, "Company1", TripStatus.ACTIVE);

		repository.save(trip);

		Optional<Trip> foundById = repository.findById(id);
		Optional<Trip> foundActive = repository.findActiveTripByPrimaryAccountNumber(pan);

		assertTrue(foundById.isPresent());
		assertTrue(foundActive.isPresent());
		assertEquals(trip, foundActive.get());
	}

	// --------------------------------------------
	// save non-ACTIVE trip removes index
	// --------------------------------------------
	@Test
	void givenTripStatusChangeToInactiveWhenSaveThenSaveAndRemoveActiveIndex() {
		var id = UUID.randomUUID();
		var pan = "5500005555555559";

		var trip = new BusTrip(id, ZonedDateTime.now(), "Stop1", "Bus1", pan, "Company1", TripStatus.ACTIVE);

		repository.save(trip);

		// Change status to COMPLETED
		trip.setStatus(TripStatus.COMPLETED);

		repository.save(trip);

		Optional<Trip> active = repository.findActiveTripByPrimaryAccountNumber(pan);

		assertTrue(active.isEmpty());
	}

	// --------------------------------------------
	// findActive returns empty when none exists
	// --------------------------------------------
	@Test
	void givenNoActiveTripExistsWhenFindActiveTripByPrimaryAccountNumberThenReturnEmpty() {
		Optional<Trip> result = repository.findActiveTripByPrimaryAccountNumber("9999");
		assertTrue(result.isEmpty());
	}

	// --------------------------------------------
	// remove removes trip and index
	// --------------------------------------------
	@Test
	void whenRemoveThenRemoveTripAndRemoveFromActiveIndex() {
		var id = UUID.randomUUID();
		var pan = "1234";

		var trip = new BusTrip(id, ZonedDateTime.now(), "Stop1", "Bus1", pan, "Company1", TripStatus.ACTIVE);

		repository.save(trip);

		repository.remove(trip);

		assertTrue(repository.findById(id).isEmpty());
		assertTrue(repository.findActiveTripByPrimaryAccountNumber(pan).isEmpty());
	}

	// --------------------------------------------
	// saving new ACTIVE trip replaces previous index
	// --------------------------------------------
	@Test
	void whenSaveThenReplaceActiveTripForSamePan() {

		String pan = "1234";

		UUID id1 = UUID.randomUUID();
		var trip1 = new BusTrip(id1, ZonedDateTime.now(), "Stop1", "Bus1", pan, "Company1", TripStatus.ACTIVE);

		UUID id2 = UUID.randomUUID();
		var trip2 = new BusTrip(id2, ZonedDateTime.now(), "Stop2", "Bus2", pan, "Company2", TripStatus.ACTIVE);

		repository.save(trip1);
		repository.save(trip2);

		Optional<Trip> active = repository.findActiveTripByPrimaryAccountNumber(pan);

		assertTrue(active.isPresent());
		assertEquals(trip2, active.get());
	}
}