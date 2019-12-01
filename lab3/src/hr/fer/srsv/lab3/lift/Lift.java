package hr.fer.srsv.lab3.lift;

import java.util.List;

import hr.fer.srsv.lab3.enums.Direction;
import hr.fer.srsv.lab3.enums.DoorStatus;
import hr.fer.srsv.lab3.traveler.Traveler;

public class Lift {

	private Integer capacity;
	private Integer position;
	private Direction direction;
	private List<Traveler> travelers;
	private Integer movingSpeed;
	private DoorStatus doorStatus;

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(final Integer capacity) {
		this.capacity = capacity;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(final Integer position) {
		this.position = position;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(final Direction direction) {
		this.direction = direction;
	}

	public List<Traveler> getTravelers() {
		return travelers;
	}

	public void setTravelers(final List<Traveler> travelers) {
		this.travelers = travelers;
	}

	public Integer getMovingSpeed() {
		return movingSpeed;
	}

	public void setMovingSpeed(final Integer movingSpeed) {
		this.movingSpeed = movingSpeed;
	}

	public DoorStatus getDoorStatus() {
		return doorStatus;
	}

	public void setDoorStatus(final DoorStatus doorStatus) {
		this.doorStatus = doorStatus;
	}
}
