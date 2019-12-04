package hr.fer.srsv.lab3.lift;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import hr.fer.srsv.lab3.enums.Direction;
import hr.fer.srsv.lab3.enums.DoorStatus;
import hr.fer.srsv.lab3.traveler.Traveler;

public class Lift {

	private final Integer capacity;
	private int position;
	private Direction direction;
	private final List<Traveler> travelers;
	private Integer movingSpeed;
	private DoorStatus doorStatus;
	private State state;
	private final List<Request> floorRequests;
	private final List<Request> innerRequests;
	private final Queue<Request> newRequests;
	private final List<Request> handlingRequests;

	public Lift(final Integer capacity) {
		this.capacity = capacity;
		direction = Direction.NONE;
		travelers = new ArrayList<>();
		doorStatus = DoorStatus.CLOSED;
		floorRequests = new ArrayList<>();
		innerRequests = new ArrayList<>();
		handlingRequests = new ArrayList<>();
		newRequests = new LinkedList<>();
	}

	public List<Traveler> getTravelers() {
		return travelers;
	}

	public List<Request> getHandlingRequests() {
		return handlingRequests;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(final Direction direction) {
		this.direction = direction;
	}

	public DoorStatus getDoorStatus() {
		return doorStatus;
	}

	public void setDoorStatus(final DoorStatus doorStatus) {
		this.doorStatus = doorStatus;
	}

	public int getPosition() {
		return position;
	}

	public List<Request> getInnerRequests() {
		return innerRequests;
	}

	public void add(final Request request) {
		newRequests.add(request);
	}

	public boolean hasRequests() {
		return hasNewRequests() || hasInnerRequests() || hasFloorRequests();
	}

	public boolean hasNewRequests() {
		return !(newRequests.isEmpty());
	}

	public boolean hasFloorRequests() {
		return !(floorRequests.isEmpty());
	}

	public boolean hasInnerRequests() {
		return !(innerRequests.isEmpty());
	}

	private enum State {
		STOPPED_OPEN, STOPPED_CLOSED, MOVING_UP, MOVING_DOWN;
	}

	public void handleNewRequest() {
		Request newRequest = newRequests.poll();
		if (newRequest == null) {
			return;
		}

		if (newRequest.getRequestType().equals(Request.Type.FLOOR)) {
			handlingRequests.add(newRequest);
		}

	}

	/**
	 * Set direction according to new request
	 */
	public void setDirection() {
		Request handlingRequest = handlingRequests.get(0);
		if (this.direction.equals(Direction.NONE)) {
			int floorSource = handlingRequest.getFloorSource();
			if (toPosition(floorSource) > position) {
				direction = Direction.UP;
			} else if (toPosition(floorSource) < position) {
				direction = Direction.DOWN;
			}
		}
	}

	public void stoppedAndDoorClosed() {
		direction = Direction.NONE;
		doorStatus = DoorStatus.CLOSED;
	}

	public void move() {
		if (direction.equals(Direction.DOWN)) {
			position -= 1;
		} else if (direction.equals(Direction.UP)) {
			position += 1;
		}
	}

	public boolean reachedDestination() {
		int floorSource = handlingRequests.get(0).getFloorSource();
		return toPosition(floorSource) == position;
	}

	private int toPosition(final int floor) {
		return 2 * floor;
	}
}
