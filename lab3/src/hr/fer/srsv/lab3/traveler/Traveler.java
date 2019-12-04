package hr.fer.srsv.lab3.traveler;

import hr.fer.srsv.lab3.LiftSystem;
import hr.fer.srsv.lab3.enums.Direction;
import hr.fer.srsv.lab3.lift.Lift;
import hr.fer.srsv.lab3.lift.RequestDisposer;

public class Traveler {

	private final String id;
	private final Integer sourceLocation;
	private final Integer destinationLocation;

	private final RequestDisposer requestDisposer;

	private Lift lift;

	public String getId() {
		return id;
	}

	public Integer getSourceLocation() {
		return sourceLocation;
	}

	public void sendRequest() {
		Direction direction = destinationLocation > sourceLocation ? Direction.UP : Direction.DOWN;
		requestDisposer.acknowledge(this.sourceLocation, direction);
	}

	public Traveler(final String id, final Integer sourceLocation, final Integer destinationLocation) {
		super();
		this.id = id;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		requestDisposer = LiftSystem.getDisposer();
	}

}
