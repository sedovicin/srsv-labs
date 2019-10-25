package hr.fer.srsv.road;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import hr.fer.srsv.enums.Direction;
import hr.fer.srsv.enums.Light;
import hr.fer.srsv.movables.Movable;

public class Lane<T extends Movable> {

	private final int length;
	private final int nonCrossingSize;
	/**
	 * Movable object is the key, object's first character's position on the lane is
	 * the value. <br/>
	 * Oldest object is first in the list, newest is last (biggest index).
	 */
	private final LinkedList<Map.Entry<T, Integer>> movables;
	private final TrafficLight trafficLight;
	private final int trafficLightPosition;
	private final Direction direction;

	public Lane(final int nonCrossingSize, final int crossingSize, final TrafficLight trafficLight,
			final Direction direction) {
		movables = new LinkedList<>();
		this.nonCrossingSize = nonCrossingSize;
		this.length = (nonCrossingSize * 2) + crossingSize;
		this.trafficLight = trafficLight;
		this.trafficLightPosition = nonCrossingSize - 1;

		this.direction = direction;
	}

	public boolean addMovable(final T movable) {
		int requiredLength = movable.getLength();
		if (canAddAnotherMovable(requiredLength)) {
			addToLane(movable);
			return true;
		}
		return false;
	}

	/**
	 * Informs every object on this lane that a step has occured and does the
	 * necessary moving
	 */
	public T simulationStep() {
		T movableToRemove = null;
		for (int index = 0; index < movables.size(); ++index) {
			if (checkTrafficLight(index)) {
				if (checkMovableInFront(index)) {
					move(index);
				}
			} else {
				stop(index);
			}

			if (outsideOfSimulation(index)) {
				movableToRemove = movables.remove(index).getKey();
				--index;
			}
		}
		return movableToRemove;
	}

	public Direction getDirection() {
		return direction;
	}

	private boolean outsideOfSimulation(final int index) {
		Entry<T, Integer> currentMovable = movables.get(index);
		if (currentMovable.getValue().compareTo(length) >= 0) {
			return true;
		}
		return false;
	}

	private boolean checkTrafficLight(final int index) {
		Entry<T, Integer> currentMovable = movables.get(index);
		if (currentMovable.getValue().equals(trafficLightPosition)) { // je li semafor ispred mene
			if (trafficLight.getLight().equals(Light.RED)) { // je li crveno
				return false;
			}
		}
		return true;
	}

	private boolean checkMovableInFront(final int index) {
		Entry<T, Integer> currentMovable = movables.get(index);
		// je li netko ispred mene, ako je, false
		if (index > 0) { // ako nisam prvi prvi
			Entry<T, Integer> movableInFrontOfCurrent = movables.get(index - 1);
			int positionBehindFrontMovable = movableInFrontOfCurrent.getValue()
					- movableInFrontOfCurrent.getKey().getLength();
			if (currentMovable.getValue() == positionBehindFrontMovable) {
				return false;
			}
		}
		return true;
	}

	private void move(final int index) {
		Entry<T, Integer> movableEntry = movables.get(index);
		Movable movable = movableEntry.getKey();
		if (movable.move()) {
			movableEntry.setValue(movableEntry.getValue() + 1);
		}
	}

	private void stop(final int index) {
		Entry<T, Integer> movableEntry = movables.get(index);
		movableEntry.getKey().stop();
	}

	private boolean canAddAnotherMovable(final int requiredLength) {
		if (movables.isEmpty()) {
			return true;
		}
		Entry<T, Integer> lastMovable = movables.get(movables.size() - 1);
		if ((lastMovable.getValue() - lastMovable.getKey().getLength()) < (requiredLength - 1)) {
			return false;
		}
		return true;
	}

	public boolean isNonCrossingAreaFilled(final int minimumMovableLength) {
		int filledPositions = 0;
		for (int i = 0; i < movables.size(); ++i) {
			if (movables.get(i).getValue().compareTo(trafficLightPosition) <= 0) {
				filledPositions += movables.get(i).getKey().getLength();
			}
		}
		if ((nonCrossingSize - filledPositions) < minimumMovableLength) {
			return true;
		}
		return false;

	}

	private void addToLane(final T movable) {
		movables.add(new AbstractMap.SimpleEntry<>(movable, movable.getLength() - 1));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < movables.size(); ++i) {
			Entry<T, Integer> movable = movables.get(i);
			int emptySpaces = length - sb.length() - 1 - movable.getValue();
			for (int j = 0; j < emptySpaces; ++j) {
				sb.append(" ");
			}
			sb.append(movable.getKey().toString());
		}
		while (sb.length() < length) {
			sb.append(" ");
		}
		if (direction.equals(Direction.UP_TO_DOWN) || direction.equals(Direction.LEFT_TO_RIGHT)) {
			sb.reverse();
		}
		return sb.toString();
	}
}
