package hr.fer.srsv.lab4.lift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.srsv.lab4.enums.Direction;
import hr.fer.srsv.lab4.enums.DoorStatus;
import hr.fer.srsv.lab4.enums.RequestType;
import hr.fer.srsv.lab4.floor.Floor;
import hr.fer.srsv.lab4.traveler.Traveler;

public class Lift implements Comparable<Lift> {
	private final Integer capacity;
	private int position;
	private Direction direction;
	private boolean moving;
	private final List<Traveler> travelers;
	private final List<Floor> floors;
	private DoorStatus doorStatus;
	private final List<Request> floorRequests;
	private final List<Request> innerRequests;

	public Lift(final Integer capacity, final List<Floor> floors) {
		this.capacity = capacity;
		direction = Direction.NONE;
		moving = false;
		travelers = new ArrayList<>();
		doorStatus = DoorStatus.CLOSED;
		floorRequests = new ArrayList<>();
		innerRequests = new ArrayList<>();
		this.floors = floors;
	}

	public List<Traveler> getTravelers() {
		return travelers;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public Direction getDirection() {
		return direction;
	}

	public DoorStatus getDoorStatus() {
		return doorStatus;
	}

	public int getPosition() {
		return position;
	}

	public List<Request> getInnerRequests() {
		return innerRequests;
	}

	public void addFloorRequest(final Request request) {
		floorRequests.add(request);
		Collections.sort(floorRequests);
	}

	public void checkAndSetNewDirection() {
		if (!floorRequests.isEmpty()) {
			Request request = floorRequests.get(0);
			direction = determineDirection(request);
		}

	}

	private Direction determineDirection(final Request request) {
		Direction newDirection = null;
		int floorPosition = request.getPosition();
		if (floorPosition > position) {
			newDirection = Direction.UP;
		} else if (floorPosition < position) {
			newDirection = Direction.DOWN;
		} else {
			newDirection = request.getDirection();
		}
		return newDirection;
	}

	public boolean atSemiFloor() {
		return (position % 2) == 1;
	}

	public void move() {
		if (direction.equals(Direction.DOWN)) {
			position -= 1;
		} else if (direction.equals(Direction.UP)) {
			position += 1;
		}
	}

	public List<Request> filterRequestsForCurrentFloor() {
		List<Request> currentFloorRequests;
		currentFloorRequests = new ArrayList<>();
		for (Request request : innerRequests) {
			if (request.getPosition().equals(position)) {
				currentFloorRequests.add(request);
			}
		}
		for (Request request : floorRequests) {
			if (request.getPosition().equals(position)) {
				currentFloorRequests.add(request);
			}
		}
		return currentFloorRequests;
	}

	public boolean isMoving() {
		return moving;
	}

	public void stop() {
		moving = false;
	}

	public void start() {
		moving = true;
	}

	public boolean shouldStop() {
		List<Request> requestsForCurrentFloor = filterRequestsForCurrentFloor();
		if (isFull() && hasRequestsForOut(requestsForCurrentFloor)) {
			return true;
		}

		return hasRequestsForOut(requestsForCurrentFloor)
				|| (!isFull() && hasRequestsForInAndCurrentDirection(requestsForCurrentFloor)) || !hasFurtherRequests();
	}

	public boolean areDoorClosed() {
		return doorStatus.equals(DoorStatus.CLOSED);
	}

	public void openDoor() {
		doorStatus = DoorStatus.OPEN;
	}

	public void closeDoor() {
		doorStatus = DoorStatus.CLOSED;
	}

	public Floor getCurrentFloor() {
		return floors.get(getCurrentFloorIndex());
	}

	public int getCurrentFloorIndex() {
		return position / 2;
	}

	public boolean isFull() {
		return travelers.size() == capacity;
	}

	public boolean shouldOpenDoor() {
		List<Request> requestsForCurrentFloor = filterRequestsForCurrentFloor();
		if (isFull() && !hasRequestsForOut(requestsForCurrentFloor)) {
			return false;
		}
		return hasRequestsForOut(requestsForCurrentFloor)
				|| (!isFull() && hasRequestsForInAndCurrentDirection(requestsForCurrentFloor))
				|| (!hasFurtherRequests() && hasAnyRequest(requestsForCurrentFloor));
	}

	public boolean hasAnyRequest(final List<Request> filteredRequests) {
		return !filteredRequests.isEmpty();
	}

	public boolean hasRequestsForOut(final List<Request> filteredRequests) {
		for (Request request : filteredRequests) {
			if (request.getRequestType().equals(RequestType.INNER)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasRequestsForOut() {
		final List<Request> requestsForCurrentFloor = filterRequestsForCurrentFloor();
		for (Request request : requestsForCurrentFloor) {
			if (request.getRequestType().equals(RequestType.INNER) && request.getPosition().equals(position)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasRequestsForInAndCurrentDirection(final List<Request> filteredRequests) {
		for (Request request : filteredRequests) {
			if (request.getDirection().equals(direction)) {
				return true;
			}
		}
		return false;
	}

	public Traveler freeTraveler() {
		for (Traveler traveler : travelers) {
			if (traveler.getDestinationFloor().equals(getCurrentFloorIndex())) {
				Request request = new Request(RequestType.INNER, getCurrentFloorIndex(), direction);
				if (innerRequests.contains(request)) {
					innerRequests.remove(request);
				} else {
					request = new Request(RequestType.INNER, getCurrentFloorIndex(), otherDirection(direction));
					innerRequests.remove(request);
				}
				travelers.remove(traveler);
				return traveler;
			}
		}
		return null;
	}

	public void addTraveler(final Traveler traveler) {
		if (traveler != null) {
			travelers.add(traveler);
			floorRequests.remove(new Request(RequestType.FLOOR, getCurrentFloorIndex(), traveler.getDirection()));
			innerRequests.add(new Request(RequestType.INNER, traveler.getDestinationFloor(), traveler.getDirection()));
		}
	}

	public List<Request> filterUpperRequests() {
		List<Request> upperRequests = new ArrayList<>();
		for (Request request : innerRequests) {
			if (request.getPosition().compareTo(position) > 0) {
				upperRequests.add(request);
			}
		}
		for (Request request : floorRequests) {
			if (request.getPosition().compareTo(position) > 0) {
				upperRequests.add(request);
			}
		}
		return upperRequests;
	}

	public List<Request> filterUpperRequests(final int floor) {
		List<Request> upperRequests = new ArrayList<>();
		for (Request request : innerRequests) {
			if (request.getFloor().compareTo(floor) > 0) {
				upperRequests.add(request);
			}
		}
		for (Request request : floorRequests) {
			if (request.getFloor().compareTo(floor) > 0) {
				upperRequests.add(request);
			}
		}
		return upperRequests;
	}

	public List<Request> filterLowerRequests() {
		List<Request> lowerRequests = new ArrayList<>();
		for (Request request : innerRequests) {
			if (request.getPosition().compareTo(position) < 0) {
				lowerRequests.add(request);
			}
		}
		for (Request request : floorRequests) {
			if (request.getPosition().compareTo(position) < 0) {
				lowerRequests.add(request);
			}
		}
		return lowerRequests;
	}

	public List<Request> filterLowerRequests(final int floor) {
		List<Request> lowerRequests = new ArrayList<>();
		for (Request request : innerRequests) {
			if (request.getFloor().compareTo(floor) < 0) {
				lowerRequests.add(request);
			}
		}
		for (Request request : floorRequests) {
			if (request.getFloor().compareTo(floor) < 0) {
				lowerRequests.add(request);
			}
		}
		return lowerRequests;
	}

	public boolean hasFurtherRequests() {
		List<Request> furtherRequests;
		if (direction.equals(Direction.UP)) {
			furtherRequests = filterUpperRequests();
		} else if (direction.equals(Direction.DOWN)) {
			furtherRequests = filterLowerRequests();
		} else {
			furtherRequests = filterRequestsForCurrentFloor();
		}

		return !furtherRequests.isEmpty();
	}

	public boolean hasFurtherRequests(final int floor) {
		List<Request> furtherRequests;
		if (direction.equals(Direction.UP)) {
			furtherRequests = filterUpperRequests(floor);
		} else if (direction.equals(Direction.DOWN)) {
			furtherRequests = filterLowerRequests(floor);
		} else {
			furtherRequests = filterRequestsForCurrentFloor();
		}

		return !furtherRequests.isEmpty();
	}

	public boolean hasBehindRequests() {
		List<Request> behindRequests = new ArrayList<>();
		if (direction.equals(Direction.DOWN)) {
			behindRequests = filterUpperRequests();
		} else if (direction.equals(Direction.UP)) {
			behindRequests = filterLowerRequests();
		}
		behindRequests.addAll(filterRequestsForCurrentFloor());
		return !behindRequests.isEmpty();
	}

	public void changeDirection() {
		if (direction.equals(Direction.DOWN)) {
			direction = Direction.UP;
		} else if (direction.equals(Direction.UP)) {
			direction = Direction.DOWN;
		}
	}

	private Direction otherDirection(final Direction direction) {
		if (direction.equals(Direction.DOWN)) {
			return Direction.UP;
		} else if (direction.equals(Direction.UP)) {
			return Direction.DOWN;
		} else {
			return Direction.NONE;
		}
	}

	@Override
	public int compareTo(final Lift o) {
		return this.capacity.compareTo(o.capacity);
	}
}
