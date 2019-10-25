package hr.fer.srsv.lab1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hr.fer.srsv.enums.Direction;
import hr.fer.srsv.enums.PedestrianDirection;
import hr.fer.srsv.enums.VehicleSize;
import hr.fer.srsv.factories.PedestrianFactory;
import hr.fer.srsv.factories.VehicleFactory;
import hr.fer.srsv.movables.Pedestrian;
import hr.fer.srsv.movables.Vehicle;
import hr.fer.srsv.road.Cycle;
import hr.fer.srsv.road.Lane;
import hr.fer.srsv.road.TrafficLight;

public class SimpleIntersection {
	private final int pedestrianNonCrossingSize = 3;
	private final int pedestrianCrossingSize = 3;
	private final int vehicleNonCrossingSize = 10;
	private final int vehicleCrossingSize = 5;

	private final int stepInterval;

	private final Cycle trafficLightCycle;

	private final Lane<Pedestrian> pedestrianLaneUD;
	private final Lane<Pedestrian> pedestrianLaneDU;
	private final Lane<Vehicle> vehicleLaneLR;
	private final Lane<Vehicle> vehicleLaneRL;
	private final List<Lane<Pedestrian>> pedestrianLanes;
	private final List<Lane<Vehicle>> vehicleLanes;
	private final TrafficLight pedestrianTrafficLight;
	private final TrafficLight vehicleTrafficLight;
	private final List<TrafficLight> trafficLights;

	private final List<Pedestrian> pedestrians;
	private final Map<VehicleSize, List<Vehicle>> vehicles;

	private final Random random;

	public SimpleIntersection(final int stepInterval, final int redAllInterval, final int greenOneDirectionInterval) {
		this.stepInterval = stepInterval;

		pedestrianTrafficLight = new TrafficLight();
		vehicleTrafficLight = new TrafficLight();
		pedestrianLaneUD = new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSize, pedestrianTrafficLight,
				Direction.UP_TO_DOWN);
		pedestrianLaneDU = new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSize, pedestrianTrafficLight,
				Direction.DOWN_TO_UP);
		vehicleLaneLR = new Lane<>(vehicleNonCrossingSize, vehicleCrossingSize, vehicleTrafficLight,
				Direction.LEFT_TO_RIGHT);
		vehicleLaneRL = new Lane<>(vehicleNonCrossingSize, vehicleCrossingSize, vehicleTrafficLight,
				Direction.RIGHT_TO_LEFT);

		pedestrianLanes = new ArrayList<>();
		vehicleLanes = new ArrayList<>();

		pedestrianLanes.add(pedestrianLaneDU);
		pedestrianLanes.add(pedestrianLaneUD);
		vehicleLanes.add(vehicleLaneLR);
		vehicleLanes.add(vehicleLaneRL);

		trafficLights = new ArrayList<>();
		trafficLights.add(pedestrianTrafficLight);
		trafficLights.add(vehicleTrafficLight);

		trafficLightCycle = new Cycle(trafficLights, redAllInterval, greenOneDirectionInterval);

		random = new Random();
		pedestrians = new ArrayList<>();
		vehicles = new HashMap<>();
		vehicles.put(VehicleSize.MOTOR, new ArrayList<>());
		vehicles.put(VehicleSize.CAR, new ArrayList<>());
		vehicles.put(VehicleSize.VAN, new ArrayList<>());
		vehicles.put(VehicleSize.TRUCK, new ArrayList<>());

	}

	public void run() {
		while (true) {
			probablyAddMovable();
			manageTrafficLights();
			moveVehicles();
			print();
			sleep();
		}

	}

	private void probablyAddMovable() {
		int laneNumber = random.nextInt(4);

		if (laneNumber < 2) {
			if (pedestrians.size() >= 50) {
				return;
			}
			Lane<Pedestrian> lane = pedestrianLanes.get(laneNumber);
			Direction direction = lane.getDirection();
			PedestrianDirection pedestrianDirection;
			if (direction.equals(Direction.LEFT_TO_RIGHT) || direction.equals(Direction.RIGHT_TO_LEFT)) {
				pedestrianDirection = PedestrianDirection.LR;
			} else {
				pedestrianDirection = PedestrianDirection.UD;
			}
			int minPedestrianSpeed = Math.floorDiv(trafficLightCycle.getRedAllInterval() / (pedestrianCrossingSize + 1),
					stepInterval);

			Pedestrian newPedestrian = PedestrianFactory.getInstance().createPedestrian(pedestrianDirection,
					minPedestrianSpeed);
			if (pedestrians.contains(newPedestrian)) {
				return;
			}
			if (lane.addMovable(newPedestrian)) {
				pedestrians.add(newPedestrian);
			}
		} else {
			laneNumber -= 2;
			Lane<Vehicle> lane = vehicleLanes.get(laneNumber);
			int minVehicleSpeed = Math.floorDiv(trafficLightCycle.getRedAllInterval() / (vehicleCrossingSize + 1),
					stepInterval);
			Vehicle newVehicle = VehicleFactory.getInstance().createVehicle(lane.getDirection().getValue(),
					minVehicleSpeed);
			if (vehicles.get(VehicleSize.getByLength(newVehicle.getLength())).contains(newVehicle)) {
				return;
			}
			if (lane.addMovable(newVehicle)) {
				vehicles.get(VehicleSize.getByLength(newVehicle.getLength())).add(newVehicle);
			}
		}
	}

	private void manageTrafficLights() {
		trafficLightCycle.step(stepInterval);
	}

	private void moveVehicles() {
		for (Lane<Pedestrian> pedestrianLane : pedestrianLanes) {
			Pedestrian pedestrianToRemove = pedestrianLane.simulationStep();
			if (pedestrianToRemove != null) {
				pedestrians.remove(pedestrianToRemove);
			}
		}
		for (Lane<Vehicle> vehicleLane : vehicleLanes) {
			Vehicle vehicleToRemove = vehicleLane.simulationStep();
			if (vehicleToRemove != null) {
				vehicles.get(VehicleSize.getByLength(vehicleToRemove.getLength())).remove(vehicleToRemove);
			}
		}
	}

	public void print() {
		int linesCount = (pedestrianNonCrossingSize * 2) + pedestrianCrossingSize;
		List<StringBuilder> lines = new ArrayList<>();
		// Create empty lines
		for (int i = 0; i < linesCount; ++i) {
			lines.add(new StringBuilder());
		}

		for (int i = 0; i < linesCount; ++i) {

			if ((i == (pedestrianNonCrossingSize - 1)) || (i == (pedestrianNonCrossingSize + pedestrianCrossingSize))) { // Road
																															// edges
				for (int j = 0; j < vehicleNonCrossingSize; ++j) {
					lines.get(i).append("=");
				}
				for (int j = 0; j < vehicleCrossingSize; ++j) {
					lines.get(i).append(" ");
				}
				for (int j = 0; j < vehicleNonCrossingSize; ++j) {
					lines.get(i).append("=");
				}
			} else if (i == (pedestrianNonCrossingSize + (pedestrianCrossingSize / 2))) { // Middle of the road
				for (int j = 0; j < ((vehicleNonCrossingSize * 2) + vehicleCrossingSize); ++j) {
					if ((j % 2) == 0) {
						lines.get(i).append(" ");
					} else {
						lines.get(i).append("-");
					}
				}

			} else if (i == pedestrianNonCrossingSize) { // right to left vehicle lane
				lines.get(i).append(vehicleLaneRL.toString());
			} else if (i == ((pedestrianNonCrossingSize + pedestrianCrossingSize) - 1)) { // left to right vehicle lane
				lines.get(i).append(vehicleLaneLR.toString());
			} else { // sides of the road
				for (int j = 0; j < ((vehicleNonCrossingSize * 2) + vehicleCrossingSize); ++j) {
					lines.get(i).append(" ");
				}
			}
		}

		String pedestrianLineUD = pedestrianLaneUD.toString();
		String pedestrianLineDU = pedestrianLaneDU.toString();

		for (int i = 0; i < linesCount; ++i) {
			if ((i >= (pedestrianNonCrossingSize - 1))
					&& (i <= ((pedestrianNonCrossingSize + pedestrianCrossingSize)))) {

				char crossingUD = lines.get(i).charAt(vehicleNonCrossingSize + 1);
				if ((crossingUD == ' ') || (crossingUD == '-')) {
					lines.get(i).setCharAt(vehicleNonCrossingSize + 1, pedestrianLineUD.charAt(i));
					if (lines.get(i).charAt(vehicleNonCrossingSize + 1) == ' ') {
						lines.get(i).setCharAt(vehicleNonCrossingSize + 1, '-');
					}
				}
				char crossingDU = lines.get(i).charAt((vehicleNonCrossingSize + vehicleCrossingSize) - 2);
				if ((crossingDU == ' ') || (crossingDU == '-')) {
					lines.get(i).setCharAt((vehicleNonCrossingSize + vehicleCrossingSize) - 2,
							pedestrianLineDU.charAt(i));
					if (lines.get(i).charAt((vehicleNonCrossingSize + vehicleCrossingSize) - 2) == ' ') {
						lines.get(i).setCharAt((vehicleNonCrossingSize + vehicleCrossingSize) - 2, '-');
					}
				}
			} else {
				lines.get(i).setCharAt(vehicleNonCrossingSize + 1, pedestrianLineUD.charAt(i));
				lines.get(i).setCharAt((vehicleNonCrossingSize + vehicleCrossingSize) - 2, pedestrianLineDU.charAt(i));
			}

		}

		for (int i = 0; i < linesCount; ++i) {
			System.out.println(lines.get(i).toString());
		}

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < ((vehicleNonCrossingSize * 2) + vehicleCrossingSize); ++j) {
			sb.append("#");
		}
		System.out.println(sb.toString());
	}

	private void sleep() {
		try {
			Thread.sleep(stepInterval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
