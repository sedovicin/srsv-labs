package hr.fer.srsv.lab3.lift;

import hr.fer.srsv.lab3.enums.Direction;

public class Request {

	private final Type requestType;
	private final Integer floorSource;
	private final Direction direction;
	private final Integer floorDestination;

	public Request(final Type requestType, final Integer floorSource, final Direction direction) {
		super();
		this.requestType = requestType;
		this.floorSource = floorSource;
		this.direction = direction;
		this.floorDestination = null;
	}

	public Type getRequestType() {
		return requestType;
	}

	public Integer getFloorSource() {
		return floorSource;
	}

	public Direction getDirection() {
		return direction;
	}

	public Integer getFloorDestination() {
		return floorDestination;
	}

	public enum Type {
		FLOOR, INNER;
	}
}
