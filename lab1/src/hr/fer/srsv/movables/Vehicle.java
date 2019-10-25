package hr.fer.srsv.movables;

import java.util.Objects;

public class Vehicle extends Movable {

	private final Character direction; // direction to, so if going from left to right should be >
	private final String vehicleString;

	public Vehicle(final String id, final int length, final int speed, final Character direction) {
		super(id, speed, length);

		this.direction = direction;
		vehicleString = buildVehicleString();
	}

	private String buildVehicleString() {
		StringBuilder sb = new StringBuilder();
		sb.append(direction);
		while ((sb.length() + id.length()) < length) {
			sb.append("*");
		}
		sb.append(id);
		return sb.toString();
	}

	@Override
	public String toString() {
		return vehicleString;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Vehicle)) {
			return false;
		}
		Vehicle other = (Vehicle) obj;
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
