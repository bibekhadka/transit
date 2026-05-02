package transit.trips.bus.service;

import transit.trips.core.model.Tap;
import transit.trips.core.repository.TripRepository;
import transit.trips.core.service.TripMapper;
import transit.trips.core.service.TripService;

public class BusTripService implements TripService {

	private final TripMapper mapper;
	private final TripRepository repository;

	public BusTripService(TripMapper mapper, TripRepository repository) {
		this.mapper = mapper;
		this.repository = repository;
	}

	@Override
	public void processTap(Tap tap) {
		var trips = mapper.toTrip(tap);
		trips.forEach(repository::save);
	}
}