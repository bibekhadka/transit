package transit.trips.bus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import transit.trips.core.model.Tap;
import transit.trips.core.model.TapType;
import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusTripStateMachineTest {

	@Mock
	private FarePolicy farePolicy;

	private TripStateMachine machine;

	@BeforeEach
	void setUp() {
		machine = new BusTripStateMachine();
	}

	// --------------------------------------------
	// TAP ON - existing open trip present
	// --------------------------------------------
	@Test
	void givenOpenTripExistsWhenTapOnThenMarkExistingTripIncompleteAndCreateNewTrip() {

		var pan = "5500005555555559";
		var now = ZonedDateTime.now();

		var existingTrip = mock(Trip.class);

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.ON);
		when(tap.getDateTime()).thenReturn(now);
		when(tap.getStopId()).thenReturn("Stop2");
		when(tap.getVehicleId()).thenReturn("Bus2");
		when(tap.getCompanyId()).thenReturn("Company1");
		when(tap.getPrimaryAccountNumber()).thenReturn(pan);

		var trips = machine.apply(tap, Optional.of(existingTrip), farePolicy);

		assertEquals(2, trips.size());

		verify(existingTrip).setStatus(TripStatus.INCOMPLETE);
		verify(existingTrip).processFare(farePolicy);

		var newTrip = trips.get(1);
		assertEquals("Stop2", newTrip.getFromStopId());
	}

	// --------------------------------------------
	// TAP ON - no open trip
	// --------------------------------------------
	@Test
	void givenNoOpenTripWhenTapOnThenCreateNewActiveTrip() {

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.ON);
		when(tap.getDateTime()).thenReturn(ZonedDateTime.now());
		when(tap.getStopId()).thenReturn("Stop1");
		when(tap.getVehicleId()).thenReturn("Bus1");
		when(tap.getCompanyId()).thenReturn("Company1");
		when(tap.getPrimaryAccountNumber()).thenReturn("123");

		var trips = machine.apply(tap, Optional.empty(), farePolicy);

		assertEquals(1, trips.size());
		assertEquals(TripStatus.ACTIVE, trips.get(0).getStatus());
	}

	// --------------------------------------------
	// TAP OFF - no open trip
	// --------------------------------------------
	@Test
	void givenNoOpenTripWhenTapOffThenCreateIncompleteTrip() {

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getDateTime()).thenReturn(ZonedDateTime.now());
		when(tap.getStopId()).thenReturn("Stop2");
		when(tap.getVehicleId()).thenReturn("Bus1");
		when(tap.getCompanyId()).thenReturn("Company1");
		when(tap.getPrimaryAccountNumber()).thenReturn("123");

		var trips = machine.apply(tap, Optional.empty(), farePolicy);

		assertEquals(1, trips.size());
		assertEquals(TripStatus.INCOMPLETE, trips.get(0).getStatus());
	}

	// --------------------------------------------
	// TAP OFF - open trip exists
	// --------------------------------------------
	@Test
	void givenOpenTripWhenTapOffThenCompleteTrip() {

		var start = ZonedDateTime.now();
		var end = start.plusMinutes(10);

		var existingTrip = mock(Trip.class);

		when(existingTrip.getId()).thenReturn(UUID.randomUUID());
		when(existingTrip.getStartedDateTime()).thenReturn(start);
		when(existingTrip.getFromStopId()).thenReturn("Stop1");
		when(existingTrip.getVehicleId()).thenReturn("Bus1");
		when(existingTrip.getPrimaryAccountNumber()).thenReturn("123");
		when(existingTrip.getCompanyId()).thenReturn("Company1");

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getDateTime()).thenReturn(end);
		when(tap.getStopId()).thenReturn("Stop2");

		var trips = machine.apply(tap, Optional.of(existingTrip), farePolicy);

		assertEquals(1, trips.size());

		var completedTrip = trips.get(0);

		assertEquals("Stop2", completedTrip.getToStopId());
		assertEquals(TripStatus.COMPLETED, completedTrip.getStatus());
		assertEquals(Duration.ofMinutes(10), completedTrip.getDuration());
	}
	
	// --------------------------------------------
	// Edge behaviors
	// --------------------------------------------

	@Test
	void givenSameStopWhenTapOffThenTripIsCancelled() {

		var start = ZonedDateTime.now();

		var existingTrip = mock(Trip.class);
		when(existingTrip.getId()).thenReturn(UUID.randomUUID());
		when(existingTrip.getStartedDateTime()).thenReturn(start);
		when(existingTrip.getFromStopId()).thenReturn("Stop1");
		when(existingTrip.getVehicleId()).thenReturn("Bus1");
		when(existingTrip.getPrimaryAccountNumber()).thenReturn("123");
		when(existingTrip.getCompanyId()).thenReturn("Company1");

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getDateTime()).thenReturn(start.plusMinutes(5));
		when(tap.getStopId()).thenReturn("Stop1"); // same stop

		var trips = machine.apply(tap, Optional.of(existingTrip), farePolicy);

		var result = trips.get(0);

		assertEquals(TripStatus.CANCELLED, result.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenTapTypeIsNull() {

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(null);

		assertThrows(NullPointerException.class, () -> machine.apply(tap, Optional.empty(), farePolicy));
	}

	@Test
	void givenActiveTripWhenSecondOnTapThenPreviousBecomesIncomplete() {

		var existing = mock(Trip.class);

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.ON);
		when(tap.getPrimaryAccountNumber()).thenReturn("123");
		when(tap.getDateTime()).thenReturn(ZonedDateTime.now());
		when(tap.getStopId()).thenReturn("Stop2");

		var trips = machine.apply(tap, Optional.of(existing), farePolicy);

		assertEquals(2, trips.size());

		verify(existing).setStatus(TripStatus.INCOMPLETE);
	}

	@Test
	void shouldEnsureNonNegativeDurationOnCompletion() {

		var start = ZonedDateTime.now();

		var existingTrip = mock(Trip.class);
		when(existingTrip.getFromStopId()).thenReturn("Stop1");
		when(existingTrip.getStartedDateTime()).thenReturn(start);

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getDateTime()).thenReturn(start.minusMinutes(10)); // invalid clock skew
		when(tap.getStopId()).thenReturn("Stop2");

		var trips = machine.apply(tap, Optional.of(existingTrip), farePolicy);

		assertTrue(trips.get(0).getDuration().toMinutes() <= 0);
	}

	@Test
	void givenNoActiveTripWhenOffThenTripMustBeIncomplete() {

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);

		var trips = machine.apply(tap, Optional.empty(), farePolicy);

		assertEquals(TripStatus.INCOMPLETE, trips.get(0).getStatus());
	}

	@Test
	void completedTripShouldPreserveOriginalTripId() {

		var id = UUID.randomUUID();
		var existing = mock(Trip.class);

		when(existing.getId()).thenReturn(id);
		when(existing.getStartedDateTime()).thenReturn(ZonedDateTime.now());
		when(existing.getFromStopId()).thenReturn("Stop1");
		when(existing.getVehicleId()).thenReturn("Bus1");
		when(existing.getPrimaryAccountNumber()).thenReturn("123");
		when(existing.getCompanyId()).thenReturn("Company1");

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getDateTime()).thenReturn(ZonedDateTime.now().plusMinutes(5));
		when(tap.getStopId()).thenReturn("Stop2");

		var result = machine.apply(tap, Optional.of(existing), farePolicy).get(0);

		assertEquals(id, result.getId());
	}
}