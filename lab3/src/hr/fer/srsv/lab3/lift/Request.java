package hr.fer.srsv.lab3.lift;

import java.util.Objects;

import hr.fer.srsv.lab3.enums.Direction;
import hr.fer.srsv.lab3.enums.RequestType;

public class Request implements Comparable<Request> {
	private final RequestType requestType;
	private final Integer floor;
	private final Direction direction;

	public Request(final RequestType requestType, final Integer floor, final Direction direction) {
		super();
		this.requestType = requestType;
		this.floor = floor;
		this.direction = direction;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public Integer getFloor() {
		return floor;
	}

	public Integer getPosition() {
		return floor * 2;
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public int compareTo(final Request o) {
		return this.floor.compareTo(o.floor);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Request)) {
			return false;
		}
		Request other = (Request) obj;
		return this.requestType.equals(other.requestType) && this.floor.equals(other.floor)
				&& this.direction.equals(other.direction);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestType, floor, direction);
	}
}
