package transit.trips.bus.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

import transit.trips.core.model.Trip;
import transit.trips.core.model.TripStatus;
import transit.trips.core.service.fare.FarePolicy;

public class BusTrip implements Trip {

	private UUID id;
	private ZonedDateTime startedDateTime;
	private ZonedDateTime finishedDateTime;
	private Duration duration;
	private String fromStopId;
	private String toStopId;
	private BigDecimal chargeAmount;
	private String companyId;
	private String vehicleId;
	private String primaryAccountNumber;
	private TripStatus status;

	public BusTrip(ZonedDateTime startedDateTime, String fromStopId, String vehicleId, String primaryAccountNumber,
			String companyId, TripStatus status) {
		this.id = UUID.randomUUID();
		this.startedDateTime = startedDateTime;
		this.fromStopId = fromStopId;
		this.vehicleId = vehicleId;
		this.primaryAccountNumber = primaryAccountNumber;
		this.companyId = companyId;
		this.status = status;
	}

	public BusTrip(UUID id, ZonedDateTime startedDateTime, String fromStopId, String vehicleId,
			String primaryAccountNumber, String companyId, TripStatus status) {
		this.id = id;
		this.startedDateTime = startedDateTime;
		this.fromStopId = fromStopId;
		this.vehicleId = vehicleId;
		this.primaryAccountNumber = primaryAccountNumber;
		this.companyId = companyId;
		this.status = status;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public ZonedDateTime getStartedDateTime() {
		return startedDateTime;
	}

	@Override
	public ZonedDateTime getFinishedDateTime() {
		return finishedDateTime;
	}

	@Override
	public Duration getDuration() {
		return duration;
	}

	@Override
	public String getFromStopId() {
		return fromStopId;
	}

	@Override
	public String getToStopId() {
		return toStopId;
	}

	@Override
	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}

	@Override
	public String getCompanyId() {
		return companyId;
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
	public TripStatus getStatus() {
		return status;
	}

	public void setFinishedDateTime(ZonedDateTime finishedDateTime) {
		this.finishedDateTime = finishedDateTime;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void setToStopId(String toStopId) {
		this.toStopId = toStopId;
	}

	public void setStatus(TripStatus status) {
		this.status = status;
	}

	@Override
	public void processFare(FarePolicy farePolicy) {
		this.chargeAmount = farePolicy.calculateFare(this);
	}

}
