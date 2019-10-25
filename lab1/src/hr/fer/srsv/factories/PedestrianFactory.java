package hr.fer.srsv.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hr.fer.srsv.enums.PedestrianDirection;
import hr.fer.srsv.movables.Movable;
import hr.fer.srsv.movables.Pedestrian;

public class PedestrianFactory {

	private static PedestrianFactory pf;
	private final Random random;
	private final Map<PedestrianDirection, String> lastIds;

	private PedestrianFactory() {
		random = new Random();
		lastIds = new HashMap<>();
		lastIds.put(PedestrianDirection.LR, "z");
		lastIds.put(PedestrianDirection.UD, "Z");

	}

	public static PedestrianFactory getInstance() {
		if (pf == null) {
			pf = new PedestrianFactory();
		}
		return pf;
	}

	/**
	 *
	 * @param direction
	 * @param minSpeed  minimal amount of steps to move. 1 means pedestrian moves
	 *                  with every call of {@link Movable#move()}, 10 means that 10
	 *                  calls of {@link Movable#move()} should occur to move this
	 *                  pedestrian for one field.
	 * @return
	 */
	public Pedestrian createPedestrian(final PedestrianDirection direction, final int minSpeed) {
		String lastId = lastIds.get(direction);
		String newId = nextId(lastId);
		lastIds.put(direction, newId);
		return new Pedestrian(newId, random.nextInt(minSpeed + 1), direction);
	}

	private String nextId(final String lastId) {
		char id = lastId.charAt(0);
		if (id == 'z') {
			return "a";
		}
		if (id == 'Z') {
			return "A";
		}
		++id;
		return String.valueOf(id);
	}
}
