package transit.trips.bus.service.fare;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;

import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.service.fare.FarePolicy;

@Component
public class BusFarePolicy implements FarePolicy {

	private final Map<String, Map<String, BigDecimal>> fareTable;
	
	public BusFarePolicy() {
		this(createDefaultFareTable());
	}
	
	public BusFarePolicy(Map<String, Map<String, BigDecimal>> fareTable) {
		this.fareTable = Map.copyOf(fareTable);
	}

	private static Map<String, Map<String, BigDecimal>> createDefaultFareTable() {
		return Map.of(
				"Stop1", Map.of("Stop2", new BigDecimal("3.25"), "Stop3", new BigDecimal("7.30")),
				"Stop2", Map.of("Stop1", new BigDecimal("3.25"), "Stop3", new BigDecimal("5.50")), 
				"Stop3", Map.of("Stop2", new BigDecimal("5.50"), "Stop1", new BigDecimal("7.30")));
	}

	@Override
	public BigDecimal calculateFare(Trip trip) {
		String from = trip.getFromStopId();
		if (trip.getStatus() == TripStatus.INCOMPLETE) {
			return calculateMaxFareFrom(from);
		}
		if (trip.getStatus() == TripStatus.COMPLETED) {
			String to = trip.getToStopId();
			if (from.equals(to)) {
				return BigDecimal.ZERO;
			}
			return fareTable.getOrDefault(from, Map.of()).getOrDefault(to, BigDecimal.ZERO);
		}
		return BigDecimal.ZERO;
	}

	private BigDecimal calculateMaxFareFrom(String from) {
		Map<String, BigDecimal> destinations = fareTable.getOrDefault(from, Map.of());
		return destinations.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
	}
}