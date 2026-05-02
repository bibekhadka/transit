package transit.trips.core.service.fare;

import java.math.BigDecimal;

import transit.trips.core.model.Trip;

public interface FarePolicy {

	BigDecimal calculateFare(Trip trip);

}
