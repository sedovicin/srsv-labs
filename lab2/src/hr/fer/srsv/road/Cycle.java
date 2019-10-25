package hr.fer.srsv.road;

import java.util.List;

import hr.fer.srsv.enums.Light;

public class Cycle {

	private final int redAllInterval;
	private final int greenOneDirectionInterval;

	private State currentState;
	private int timerValue;

	private final List<TrafficLight> trafficLights;

	/**
	 * Traffic light intervals in milliseconds!
	 *
	 * @param redAllInterval
	 * @param greenOneDirectionInterval
	 */
	public Cycle(final List<TrafficLight> trafficLights, final int redAllInterval,
			final int greenOneDirectionInterval) {
		this.trafficLights = trafficLights;
		this.redAllInterval = redAllInterval;
		this.greenOneDirectionInterval = greenOneDirectionInterval;

		currentState = State.RED_ALL1;
		timerValue = redAllInterval;
	}

	public int getRedAllInterval() {
		return redAllInterval;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void step(final int milliseconds, final boolean shouldMoveToNextState) {
		timerValue -= milliseconds;

		if (timerValue <= 0) {
			if (!(currentState.equals(State.RED_ALL1) || currentState.equals(State.RED_ALL2))) {
				moveToNextState();
				manageTrafficLights();
			} else {
				if (shouldMoveToNextState) {
					moveToNextState();
					manageTrafficLights();
				}
			}

		}
	}

	private void moveToNextState() {
		currentState = State.values()[(currentState.ordinal() + 1) % State.values().length];
		timerValue = currentState.equals(State.RED_ALL1) || currentState.equals(State.RED_ALL2) ? redAllInterval
				: greenOneDirectionInterval;
	}

	private void manageTrafficLights() {
		switch (currentState) {
		case RED_ALL1:
		case RED_ALL2:
			allToRed();
			break;
		case GREEN_FIRST:
			firstToGreen();
			break;
		case GREEN_SECOND:
			secondToGreen();
			break;
		default:
			allToRed();
		}
	}

	private void allToRed() {
		for (TrafficLight trafficLight : trafficLights) {
			trafficLight.setLight(Light.RED);
		}
	}

	private void firstToGreen() {
		trafficLights.get(0).setLight(Light.GREEN);
	}

	private void secondToGreen() {
		trafficLights.get(1).setLight(Light.GREEN);
	}

	public enum State {
		RED_ALL1, GREEN_FIRST, RED_ALL2, GREEN_SECOND;
	}
}
