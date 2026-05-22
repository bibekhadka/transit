package transit.trips.bus.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.repository.TripRepository;
import transit.trips.core.service.TripService;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;
import static transit.trips.core.util.MaskingUtil.maskPan;

@Service
public class BusTripService implements TripService {

	private static final Logger log = LoggerFactory.getLogger(BusTripService.class);

	private final TripStateMachine stateMachine;
	private final TripRepository repository;
	private final FarePolicy farePolicy;

	@Autowired
	public BusTripService(TripStateMachine stateMachine, TripRepository repository, FarePolicy farePolicy) {
		this.stateMachine = stateMachine;
		this.repository = repository;
		this.farePolicy = farePolicy;
	}

	@Override
	public void processTap(Tap tap) {
		log.info("Processing tap event for PAN {}, stop {}, type {}", maskPan(tap.getPrimaryAccountNumber()),
				tap.getStopId(), tap.getType());
		Optional<Trip> active = repository.findActiveTripByPrimaryAccountNumber(tap.getPrimaryAccountNumber());
		List<Trip> newState = stateMachine.apply(tap, active, farePolicy);
		newState.forEach(trip -> {
			log.info("Trip {} transitioned to status {} with fare {}", trip.getId(), trip.getStatus(), trip.getChargeAmount());
			repository.save(trip);
		});
	}
}