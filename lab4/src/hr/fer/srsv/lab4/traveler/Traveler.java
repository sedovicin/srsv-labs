package hr.fer.srsv.lab4.traveler;

import hr.fer.srsv.lab4.LiftSystem;
import hr.fer.srsv.lab4.enums.Direction;
import hr.fer.srsv.lab4.lift.Lift;
import hr.fer.srsv.lab4.lift.RequestDisposer;

public class Traveler {
	private final String id;
	private final Integer sourceFloor;
	private final Integer destinationFloor;

	private final RequestDisposer requestDisposer;

	private Lift lift;

	public String getId() {
		return id;
	}

	public Integer getSourceFloor() {
		return sourceFloor;
	}

	public Integer getDestinationFloor() {
		return destinationFloor;
	}

	public Direction getDirection() {
		return destinationFloor > sourceFloor ? Direction.UP : Direction.DOWN;
	}

	public void sendRequest() {
		Direction direction = getDirection();
		lift = requestDisposer.acknowledgeFloorRequest(this.sourceFloor, direction);
	}

	public Traveler(final String id, final Integer sourceFloor, final Integer destinationFloor) {
		super();
		this.id = id;
		this.sourceFloor = sourceFloor;
		this.destinationFloor = destinationFloor;
		requestDisposer = LiftSystem.getDisposer();
	}

	public Lift getLift() {
		return lift;
	}
}
