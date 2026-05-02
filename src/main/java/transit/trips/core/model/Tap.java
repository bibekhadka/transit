package transit.trips.core.model;

import java.time.ZonedDateTime;

public interface Tap {
	ZonedDateTime getDateTime();

	TapType getType();

	String getStopId();

	String getVehicleId();

	String getPrimaryAccountNumber();

	String getCompanyId();
}
