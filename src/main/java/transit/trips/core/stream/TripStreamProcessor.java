package transit.trips.core.stream;

import java.util.List;
import java.util.Optional;

import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;

@Service
public class TripStreamProcessor implements Processor<String, Tap, String, Trip> {

	private final TripStateMachine stateMachine;
	private final FarePolicy farePolicy;

	@Autowired
	public TripStreamProcessor(TripStateMachine stateMachine, FarePolicy farePolicy) {
		this.stateMachine = stateMachine;
		this.farePolicy = farePolicy;
	}

	private ProcessorContext<String, Trip> context;
	private KeyValueStore<String, Trip> store;

	@Override
	public void init(ProcessorContext<String, Trip> context) {
		this.context = context;
		this.store = context.getStateStore("trip-store");
	}

	@Override
	public void process(Record<String, Tap> record) {
		String primaryAccountNumber = record.key();
		Tap tap = record.value();
		Optional<Trip> active = Optional.ofNullable(store.get(primaryAccountNumber));
		List<Trip> newState = stateMachine.apply(tap, active, farePolicy);
		newState.forEach(trip -> {
			if (trip.getStatus() == TripStatus.ACTIVE) {
				store.put(trip.getPrimaryAccountNumber(), trip);
			} else {
				store.delete(trip.getPrimaryAccountNumber());
			}
			context.forward(record.withValue(trip));
		});
	}
}