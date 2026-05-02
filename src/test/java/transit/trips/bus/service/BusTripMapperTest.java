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
import transit.trips.core.repository.TripRepository;
import transit.trips.core.service.TripMapper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusTripMapperTest {

	@Mock
	private TripRepository repository;

	private TripMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new BusTripMapper(repository);
	}

	// --------------------------------------------
	// TAP ON - existing open trip present
	// --------------------------------------------
	@Test
	void givenOpenTripExistsWhenTapOnThenMarkExistingTripIncompleteAndCreateNewTrip() {

		var pan = "5500005555555559";
		var now = ZonedDateTime.now();

		var existingTrip = mock(Trip.class);

		when(repository.findActiveTripByPrimaryAccountNumber(pan)).thenReturn(Optional.of(existingTrip));

		Tap tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.ON);
		when(tap.getPrimaryAccountNumber()).thenReturn(pan);
		when(tap.getDateTime()).thenReturn(now.plusMinutes(1));
		when(tap.getStopId()).thenReturn("Stop2");
		when(tap.getVehicleId()).thenReturn("Bus2");
		when(tap.getCompanyId()).thenReturn("Company1");

		var trips = mapper.toTrip(tap);

		assertEquals(2, trips.size());
		verify(existingTrip).setStatus(TripStatus.INCOMPLETE);

		var newTrip = trips.get(1);
		assertEquals("Stop2", newTrip.getFromStopId());
		assertEquals(pan, newTrip.getPrimaryAccountNumber());
	}

	// --------------------------------------------
	// TAP ON - no open trip
	// --------------------------------------------
	@Test
	void givenNoOpenTripExistsWhenTapOnShouldCreateNewTripOnly() {

		var pan = "5500005555555559";
		var now = ZonedDateTime.now();

		when(repository.findActiveTripByPrimaryAccountNumber(pan)).thenReturn(Optional.empty());

		Tap tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.ON);
		when(tap.getPrimaryAccountNumber()).thenReturn(pan);
		when(tap.getDateTime()).thenReturn(now);
		when(tap.getStopId()).thenReturn("Stop1");
		when(tap.getVehicleId()).thenReturn("Bus1");
		when(tap.getCompanyId()).thenReturn("Company1");

		var trips = mapper.toTrip(tap);

		assertEquals(1, trips.size());
		var newTrip = trips.get(0);

		assertEquals("Stop1", newTrip.getFromStopId());
		assertEquals(pan, newTrip.getPrimaryAccountNumber());
	}

	// --------------------------------------------
	// TAP OFF - no open trip
	// --------------------------------------------
	@Test
	void givenNoOpenTripExistsWhenTapOffShouldCreateIncompleteTrip() {

		var pan = "5500005555555559";
		var now = ZonedDateTime.now();

		when(repository.findActiveTripByPrimaryAccountNumber(pan)).thenReturn(Optional.empty());

		Tap tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getPrimaryAccountNumber()).thenReturn(pan);
		when(tap.getDateTime()).thenReturn(now);
		when(tap.getStopId()).thenReturn("Stop2");
		when(tap.getVehicleId()).thenReturn("Bus1");
		when(tap.getCompanyId()).thenReturn("Company1");

		var trips = mapper.toTrip(tap);

		assertEquals(1, trips.size());
		assertEquals(TripStatus.INCOMPLETE, trips.get(0).getStatus());
	}

	// --------------------------------------------
	// TAP OFF - open trip exists
	// --------------------------------------------
	@Test
	void givenOpenTripExistsWhenTapOffShouldCompleteTrip() {

		var pan = "5500005555555559";
		var start = ZonedDateTime.now();
		var end = start.plusMinutes(10);

		var existingTrip = mock(Trip.class);

		when(repository.findActiveTripByPrimaryAccountNumber(pan)).thenReturn(Optional.of(existingTrip));
		when(existingTrip.getStartedDateTime()).thenReturn(start);
		when(existingTrip.getFromStopId()).thenReturn("Stop1");
		when(existingTrip.getVehicleId()).thenReturn("Bus1");
		when(existingTrip.getPrimaryAccountNumber()).thenReturn(pan);
		when(existingTrip.getCompanyId()).thenReturn("Company1");

		var tap = mock(Tap.class);
		when(tap.getType()).thenReturn(TapType.OFF);
		when(tap.getPrimaryAccountNumber()).thenReturn(pan);
		when(tap.getDateTime()).thenReturn(end);
		when(tap.getStopId()).thenReturn("Stop2");

		var trips = mapper.toTrip(tap);

		assertEquals(1, trips.size());

		var completedTrip = trips.get(0);

		assertEquals("Stop2", completedTrip.getToStopId());
		assertEquals(TripStatus.COMPLETED, completedTrip.getStatus());
		assertEquals(Duration.ofMinutes(10), completedTrip.getDuration());
	}
}