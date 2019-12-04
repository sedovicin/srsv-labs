package hr.fer.srsv.lab3.lift;

import hr.fer.srsv.lab3.enums.Direction;

public class Request {

	private final Type requestType;
	private final Integer floor;
	private final Direction direction;

	public Request(final Type requestType, final Integer floor, final Direction direction) {
		super();
		this.requestType = requestType;
		this.floor = floor;
		this.direction = direction;
	}

	public Type getRequestType() {
		return requestType;
	}

	public Integer getFloor() {
		return floor;
	}

	public Direction getDirection() {
		return direction;
	}

	public enum Type {
		FLOOR, INNER;
	}
}
