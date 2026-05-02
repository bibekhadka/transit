package transit.trips.bus.service;

import java.util.List;
import java.util.Optional;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.repository.TripRepository;
import transit.trips.core.service.TripService;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;

public class BusTripService implements TripService {

	private final TripStateMachine stateMachine;
	private final TripRepository repository;
	private final FarePolicy farePolicy;

	public BusTripService(TripStateMachine stateMachine, TripRepository repository, FarePolicy farePolicy) {
		this.stateMachine = stateMachine;
		this.repository = repository;
		this.farePolicy = farePolicy;
	}

	@Override
	public void processTap(Tap tap) {
		Optional<Trip> active = repository.findActiveTripByPrimaryAccountNumber(tap.getPrimaryAccountNumber());
		List<Trip> newState = stateMachine.apply(tap, active, farePolicy);
		newState.forEach(repository::save);
	}
}