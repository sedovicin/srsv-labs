package hr.fer.srsv.road;

import hr.fer.srsv.enums.Light;

public class TrafficLight {

	private Light lightOn;

	public TrafficLight() {
		lightOn = Light.RED;
	}

	public Light getLight() {
		return lightOn;
	}

	public void setLight(final Light light) {
		lightOn = light;
	}

	public void changeLight() {
		if (lightOn.equals(Light.RED)) {
			lightOn = Light.GREEN;
		}
		if (lightOn.equals(Light.GREEN)) {
			lightOn = Light.RED;
		}
	}

}
