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
}