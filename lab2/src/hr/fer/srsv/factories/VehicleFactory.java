package hr.fer.srsv.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hr.fer.srsv.enums.VehicleSize;
import hr.fer.srsv.movables.Movable;
import hr.fer.srsv.movables.Vehicle;

public class VehicleFactory {

	private static VehicleFactory vf;
	private final Random random;
	private final Map<VehicleSize, Integer> lastIds;

	public VehicleFactory() {
		random = new Random();
		lastIds = new HashMap<>();
		lastIds.put(VehicleSize.MOTOR, 9);
		lastIds.put(VehicleSize.CAR, 50);
		lastIds.put(VehicleSize.VAN, 60);
		lastIds.put(VehicleSize.TRUCK, 70);

	}

	public static VehicleFactory getInstance() {
		if (vf == null) {
			vf = new VehicleFactory();
		}
		return vf;
	}

	/**
	 *
	 * @param size
	 * @param direction
	 * @param minSpeed  minimal amount of steps to move. 1 means vehicle moves with
	 *                  every call of {@link Movable#move()}, 10 means that 10 calls
	 *                  of {@link Movable#move()} should occur to move this vehicle
	 *                  for one field.
	 * @return
	 */
	public Vehicle createVehicle(final VehicleSize size, final Character direction, final int minSpeed) {
		Integer lastId = lastIds.get(size);
		Integer newId = nextId(lastId);
		lastIds.put(size, newId);
		return new Vehicle(newId.toString(), size.getLength(), random.nextInt(minSpeed + 1), direction);
	}

	/**
	 * Create a random sized vehicle.
	 *
	 * @param direction
	 * @param minSpeed  minimal amount of steps to move. 1 means vehicle moves with
	 *                  every call of {@link Movable#move()}, 10 means that 10 calls
	 *                  of {@link Movable#move()} should occur to move this vehicle
	 *                  for one field.
	 * @return
	 */
	public Vehicle createVehicle(final Character direction, final int minSpeed) {
		int number = random.nextInt(100);
		if (number < 5) {
			return createVehicle(VehicleSize.MOTOR, direction, minSpeed);
		}
		if (number < 85) {
			return createVehicle(VehicleSize.CAR, direction, minSpeed);
		}
		if (number < 95) {
			return createVehicle(VehicleSize.VAN, direction, minSpeed);
		}
		return createVehicle(VehicleSize.TRUCK, direction, minSpeed);

	}

	private Integer nextId(final Integer lastId) {
		if (lastId.equals(9)) {
			return 1;
		}
		if (lastId.equals(50)) {
			return 10;
		}
		if (lastId.equals(60)) {
			return 51;
		}
		if (lastId.equals(70)) {
			return 61;
		}

		return lastId.intValue() + 1;

	}

}
