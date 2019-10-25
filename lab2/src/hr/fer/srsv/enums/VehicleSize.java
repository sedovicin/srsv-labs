package hr.fer.srsv.enums;

public enum VehicleSize {
	MOTOR(2), CAR(3), VAN(4), TRUCK(5);

	private int length;

	private VehicleSize(final int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public static VehicleSize getByLength(final int length) {
		for (VehicleSize vs : VehicleSize.values()) {
			if (vs.length == length) {
				return vs;
			}
		}
		return null;
	}
}
