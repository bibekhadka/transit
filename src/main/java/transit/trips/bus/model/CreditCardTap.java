package transit.trips.bus.model;

import java.time.ZonedDateTime;

import transit.trips.core.model.Tap;
import transit.trips.core.model.TapType;

public class CreditCardTap implements Tap {

	private ZonedDateTime dateTime;
	private TapType type;
	private String stopId;
	private String vehicleId;
	private String primaryAccountNumber;
	private String companyId;

	public CreditCardTap(TapType type, String stopId, String companyId, String vehicleId, String primaryAccountNumber,
			ZonedDateTime dateTime) {

	}

	@Override
	public ZonedDateTime getDateTime() {
		return dateTime;
	}

	@Override
	public TapType getType() {
		return type;
	}

	@Override
	public String getStopId() {
		return stopId;
	}

	@Override
	public String getVehicleId() {
		return vehicleId;
	}

	@Override
	public String getPrimaryAccountNumber() {
		return primaryAccountNumber;
	}

	@Override
	public String getCompanyId() {
		return companyId;
	}

}
