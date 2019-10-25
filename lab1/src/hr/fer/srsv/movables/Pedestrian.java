package hr.fer.srsv.movables;

import java.util.Objects;

import hr.fer.srsv.enums.PedestrianDirection;

public class Pedestrian extends Movable {

	private final PedestrianDirection direction;

	public Pedestrian(final String id, final int speed, final PedestrianDirection direction) {
		super(id, speed, 1);
		this.direction = direction;
	}

	public PedestrianDirection getDirection() {
		return direction;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Pedestrian)) {
			return false;
		}
		Pedestrian other = (Pedestrian) obj;
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
