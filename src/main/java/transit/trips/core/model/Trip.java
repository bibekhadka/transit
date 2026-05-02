package transit.trips.core.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface Trip {

	UUID getId();

	ZonedDateTime getStartedDateTime();

	ZonedDateTime getFinishedDateTime();

	Duration getDuration();

	String getFromStopId();

	String getToStopId();

	BigDecimal getChargeAmount();

	String getCompanyId();

	String getVehicleId();

	String getPrimaryAccountNumber();

	TripStatus getStatus();

	void setStatus(TripStatus status);

}
