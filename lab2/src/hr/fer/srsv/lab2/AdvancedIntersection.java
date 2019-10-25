package hr.fer.srsv.lab2;

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

public class AdvancedIntersection {
	private final int pedestrianNonCrossingSize = 3;
	private final int pedestrianCrossingSizeUD = 3;
	private final int pedestrianCrossingSizeLR = 7;

	private final int vehicleNonCrossingSizeUD = 10;
	private final int vehicleNonCrossingSizeLR = 20;
	private final int vehicleCrossingSizeUD = 4 + 3 + 4;
	private final int vehicleCrossingSizeLR = 8 + 7 + 8;

	private final int stepInterval;

	private final Cycle trafficLightCycle;

	private final List<Lane<Pedestrian>> pedestrianLanes;
	private final Map<Direction, Lane<Vehicle>> vehicleLanes;
	private final TrafficLight horizontalTrafficLight;
	private final TrafficLight verticalTrafficLight;
	private final List<TrafficLight> trafficLights;

	private final List<Pedestrian> pedestrians;
	private final Map<VehicleSize, List<Vehicle>> vehicles;

	private final Random random;

	public AdvancedIntersection(final int stepInterval, final int redAllInterval, final int greenOneDirectionInterval) {
		this.stepInterval = stepInterval;

		horizontalTrafficLight = new TrafficLight();
		verticalTrafficLight = new TrafficLight();

		pedestrianLanes = new ArrayList<>();
		vehicleLanes = new HashMap<>();

		// west
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeUD, verticalTrafficLight,
				Direction.UP_TO_DOWN));
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeUD, verticalTrafficLight,
				Direction.DOWN_TO_UP));
		// east
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeUD, verticalTrafficLight,
				Direction.UP_TO_DOWN));
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeUD, verticalTrafficLight,
				Direction.DOWN_TO_UP));
		// north
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeLR, horizontalTrafficLight,
				Direction.RIGHT_TO_LEFT));
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeLR, horizontalTrafficLight,
				Direction.LEFT_TO_RIGHT));
		// south
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeLR, horizontalTrafficLight,
				Direction.RIGHT_TO_LEFT));
		pedestrianLanes.add(new Lane<>(pedestrianNonCrossingSize, pedestrianCrossingSizeLR, horizontalTrafficLight,
				Direction.LEFT_TO_RIGHT));

		vehicleLanes.put(Direction.LEFT_TO_RIGHT, new Lane<>(vehicleNonCrossingSizeLR, vehicleCrossingSizeLR,
				horizontalTrafficLight, Direction.LEFT_TO_RIGHT));
		vehicleLanes.put(Direction.RIGHT_TO_LEFT, new Lane<>(vehicleNonCrossingSizeLR, vehicleCrossingSizeLR,
				horizontalTrafficLight, Direction.RIGHT_TO_LEFT));
		vehicleLanes.put(Direction.UP_TO_DOWN, new Lane<>(vehicleNonCrossingSizeUD, vehicleCrossingSizeUD,
				verticalTrafficLight, Direction.UP_TO_DOWN));
		vehicleLanes.put(Direction.DOWN_TO_UP, new Lane<>(vehicleNonCrossingSizeUD, vehicleCrossingSizeUD,
				verticalTrafficLight, Direction.DOWN_TO_UP));

		trafficLights = new ArrayList<>();
		trafficLights.add(horizontalTrafficLight);
		trafficLights.add(verticalTrafficLight);

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
		int laneNumber = random.nextInt(12);

		if (laneNumber < 8) {
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
			int larger = pedestrianCrossingSizeUD;
			if (pedestrianCrossingSizeLR > pedestrianCrossingSizeUD) {
				larger = pedestrianCrossingSizeLR;
			}
			int minPedestrianSpeed = Math.floorDiv(trafficLightCycle.getRedAllInterval() / (larger + 1), stepInterval);

			Pedestrian newPedestrian = PedestrianFactory.getInstance().createPedestrian(pedestrianDirection,
					minPedestrianSpeed);
			if (pedestrians.contains(newPedestrian)) {
				return;
			}
			if (lane.addMovable(newPedestrian)) {
				pedestrians.add(newPedestrian);
			}
		} else {
			laneNumber -= 8;
			List<Lane<Vehicle>> vehicleLanesList = new ArrayList<>(vehicleLanes.values());
			Lane<Vehicle> lane = vehicleLanesList.get(laneNumber);
			int larger = vehicleCrossingSizeUD;
			if (vehicleCrossingSizeLR > vehicleCrossingSizeUD) {
				larger = vehicleCrossingSizeLR;
			}
			int minVehicleSpeed = Math.floorDiv(trafficLightCycle.getRedAllInterval() / (larger + 1), stepInterval);
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
		for (Lane<Vehicle> vehicleLane : vehicleLanes.values()) {
			Vehicle vehicleToRemove = vehicleLane.simulationStep();
			if (vehicleToRemove != null) {
				vehicles.get(VehicleSize.getByLength(vehicleToRemove.getLength())).remove(vehicleToRemove);
			}
		}
	}

	public void print() {
		int linesCount = (vehicleNonCrossingSizeUD * 2) + vehicleCrossingSizeUD;
		List<StringBuilder> lines = new ArrayList<>();
		// Create empty lines
		for (int i = 0; i < linesCount; ++i) {
			lines.add(new StringBuilder());
		}
		// horizontal
		for (int i = 0; i < linesCount; ++i) {
			if ((i == (vehicleNonCrossingSizeUD + 3))
					|| (i == ((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 4))) { // Road
				// edges
				for (int j = 0; j < vehicleNonCrossingSizeLR; ++j) {
					lines.get(i).append("=");
				}
				for (int j = 0; j < vehicleCrossingSizeLR; ++j) {
					lines.get(i).append(" ");
				}
				for (int j = 0; j < vehicleNonCrossingSizeLR; ++j) {
					lines.get(i).append("=");
				}
			} else if (i == (vehicleNonCrossingSizeUD + (vehicleCrossingSizeUD / 2))) { // Middle of the road
				for (int j = 0; j < ((vehicleNonCrossingSizeLR * 2) + vehicleCrossingSizeLR); ++j) {
					if (((j % 2) == 1) && ((j < vehicleNonCrossingSizeLR)
							|| (j > (vehicleNonCrossingSizeLR + vehicleCrossingSizeLR)))) {
						lines.get(i).append("-");
					} else {
						lines.get(i).append(" ");
					}
				}
				lines.get(i).setCharAt((vehicleNonCrossingSizeLR + (vehicleCrossingSizeLR / 2)), '+');
			} else if (i == (vehicleNonCrossingSizeUD + 4)) { // right to left vehicle lane
			} else if (i == ((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 5)) { // left to right vehicle lane
			} else { // sides of the road
				for (int j = 0; j < ((vehicleNonCrossingSizeLR * 2) + vehicleCrossingSizeLR); ++j) {
					lines.get(i).append(" ");
				}
			}
		}

		// vertical
		for (int i = 0; i < ((vehicleNonCrossingSizeUD * 2) + vehicleCrossingSizeUD); ++i) {
			for (int j = 0; j < ((vehicleNonCrossingSizeLR * 2) + vehicleCrossingSizeLR); ++j) {
				if ((j == (vehicleNonCrossingSizeLR + 7))
						|| (j == ((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 8))) {
					if ((i < vehicleNonCrossingSizeUD) || (i >= (vehicleNonCrossingSizeUD + vehicleCrossingSizeUD))) {
						lines.get(i).setCharAt(j, 'H');
					}
				}
				if (j == (vehicleNonCrossingSizeLR + (vehicleCrossingSizeLR / 2))) {
					if ((i < vehicleNonCrossingSizeUD) || (i >= (vehicleNonCrossingSizeUD + vehicleCrossingSizeUD))) {
						lines.get(i).setCharAt(j, '|');
					}

				}
			}
		}

		// traffic
		lines.get(vehicleNonCrossingSizeUD + 4).append(vehicleLanes.get(Direction.RIGHT_TO_LEFT).toString());
		lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 5)
				.append(vehicleLanes.get(Direction.LEFT_TO_RIGHT).toString());

		String vehicleUD = vehicleLanes.get(Direction.UP_TO_DOWN).toString();
		String vehicleDU = vehicleLanes.get(Direction.DOWN_TO_UP).toString();

		for (int i = 0; i < linesCount; ++i) {
			if ((lines.get(i).charAt(vehicleNonCrossingSizeLR + 9) == ' ')
					|| (lines.get(i).charAt(vehicleNonCrossingSizeLR + 9) == '|')) {
				lines.get(i).setCharAt(vehicleNonCrossingSizeLR + 9, vehicleUD.charAt(i));
			}
			if ((lines.get(i).charAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 10) == ' ')
					|| (lines.get(i).charAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 10) == '|')) {
				lines.get(i).setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 10, vehicleDU.charAt(i));
			}
		}

		String northRL = pedestrianLanes.get(4).toString();
		String northLR = pedestrianLanes.get(5).toString();
		String southRL = pedestrianLanes.get(6).toString();
		String southLR = pedestrianLanes.get(7).toString();
		for (int i = 0; i < ((pedestrianNonCrossingSize * 2) + pedestrianCrossingSizeLR); ++i) {
			// north
			char charr = lines.get(vehicleNonCrossingSizeUD).charAt(vehicleNonCrossingSizeLR + 5 + i);
			if ((charr == ' ') || (charr == '|')) {
				lines.get(vehicleNonCrossingSizeUD).setCharAt(vehicleNonCrossingSizeLR + 5 + i, northRL.charAt(i));
				if (lines.get(vehicleNonCrossingSizeUD).charAt(vehicleNonCrossingSizeLR + 5 + i) == ' ') {
					lines.get(vehicleNonCrossingSizeUD).setCharAt(vehicleNonCrossingSizeLR + 5 + i, '|');
				}
			}
			charr = lines.get(vehicleNonCrossingSizeUD + 1).charAt(vehicleNonCrossingSizeLR + 5 + i);
			if ((charr == ' ') || (charr == '|')) {
				lines.get(vehicleNonCrossingSizeUD + 1).setCharAt(vehicleNonCrossingSizeLR + 5 + i, northLR.charAt(i));
				if (lines.get(vehicleNonCrossingSizeUD + 1).charAt(vehicleNonCrossingSizeLR + 5 + i) == ' ') {
					lines.get(vehicleNonCrossingSizeUD + 1).setCharAt(vehicleNonCrossingSizeLR + 5 + i, '|');
				}
			}

			// south
			charr = lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 2)
					.charAt(vehicleNonCrossingSizeLR + 5 + i);
			if ((charr == ' ') || (charr == '|')) {
				lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 2)
						.setCharAt(vehicleNonCrossingSizeLR + 5 + i, southRL.charAt(i));
				if (lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 2)
						.charAt(vehicleNonCrossingSizeLR + 5 + i) == ' ') {
					lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 2)
							.setCharAt(vehicleNonCrossingSizeLR + 5 + i, '|');
				}
			}
			charr = lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 1)
					.charAt(vehicleNonCrossingSizeLR + 5 + i);
			if ((charr == ' ') || (charr == '|')) {
				lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 1)
						.setCharAt(vehicleNonCrossingSizeLR + 5 + i, southLR.charAt(i));
				if (lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 1)
						.charAt(vehicleNonCrossingSizeLR + 5 + i) == ' ') {
					lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 1)
							.setCharAt(vehicleNonCrossingSizeLR + 5 + i, '|');
				}
			}

			if ((i < (pedestrianNonCrossingSize - 1))
					|| (i >= (pedestrianNonCrossingSize + pedestrianCrossingSizeLR + 1))) {
				lines.get(vehicleNonCrossingSizeUD).setCharAt(vehicleNonCrossingSizeLR + 5 + i, northRL.charAt(i));
				lines.get(vehicleNonCrossingSizeUD + 1).setCharAt(vehicleNonCrossingSizeLR + 5 + i, northLR.charAt(i));
				lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 2)
						.setCharAt(vehicleNonCrossingSizeLR + 5 + i, southRL.charAt(i));
				lines.get((vehicleNonCrossingSizeUD + vehicleCrossingSizeUD) - 1)
						.setCharAt(vehicleNonCrossingSizeLR + 5 + i, southLR.charAt(i));

			}
		}

		String westUD = pedestrianLanes.get(0).toString();
		String westDU = pedestrianLanes.get(1).toString();
		String eastUD = pedestrianLanes.get(2).toString();
		String eastDU = pedestrianLanes.get(3).toString();
		for (int i = 0; i < ((pedestrianNonCrossingSize * 2) + pedestrianCrossingSizeUD); ++i) {
			// west
			char charr = lines.get(vehicleNonCrossingSizeUD + 1 + i).charAt(vehicleNonCrossingSizeLR + 1);
			if ((charr == ' ') || (charr == '-')) {
				lines.get(vehicleNonCrossingSizeUD + 1 + i).setCharAt(vehicleNonCrossingSizeLR + 1, westUD.charAt(i));
				if (lines.get(vehicleNonCrossingSizeUD + 1 + i).charAt(vehicleNonCrossingSizeLR + 1) == ' ') {
					lines.get(vehicleNonCrossingSizeUD + 1 + i).setCharAt(vehicleNonCrossingSizeLR + 1, '-');
				}
			}

			charr = lines.get(vehicleNonCrossingSizeUD + 1 + i).charAt(vehicleNonCrossingSizeLR + 3);
			if ((charr == ' ') || (charr == '-')) {
				lines.get(vehicleNonCrossingSizeUD + 1 + i).setCharAt(vehicleNonCrossingSizeLR + 3, westDU.charAt(i));
				if (lines.get(vehicleNonCrossingSizeUD + 1 + i).charAt(vehicleNonCrossingSizeLR + 3) == ' ') {
					lines.get(vehicleNonCrossingSizeUD + 1 + i).setCharAt(vehicleNonCrossingSizeLR + 3, '-');
				}
			}
			// east
			charr = lines.get(vehicleNonCrossingSizeUD + 1 + i)
					.charAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 4);
			if ((charr == ' ') || (charr == '-')) {
				lines.get(vehicleNonCrossingSizeUD + 1 + i)
						.setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 4, eastUD.charAt(i));
				if (lines.get(vehicleNonCrossingSizeUD + 1 + i)
						.charAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 4) == ' ') {
					lines.get(vehicleNonCrossingSizeUD + 1 + i)
							.setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 4, '-');
				}
			}

			charr = lines.get(vehicleNonCrossingSizeUD + 1 + i)
					.charAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 2);
			if ((charr == ' ') || (charr == '-')) {
				lines.get(vehicleNonCrossingSizeUD + 1 + i)
						.setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 2, eastDU.charAt(i));
				if (lines.get(vehicleNonCrossingSizeUD + 1 + i)
						.charAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 2) == ' ') {
					lines.get(vehicleNonCrossingSizeUD + 1 + i)
							.setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 2, '-');
				}
			}
			if ((i < (pedestrianNonCrossingSize - 1))
					|| (i >= (pedestrianNonCrossingSize + pedestrianCrossingSizeUD + 1))) {
				lines.get(vehicleNonCrossingSizeUD + 1 + i).setCharAt(vehicleNonCrossingSizeLR + 1, westUD.charAt(i));
				lines.get(vehicleNonCrossingSizeUD + 1 + i).setCharAt(vehicleNonCrossingSizeLR + 3, westDU.charAt(i));
				lines.get(vehicleNonCrossingSizeUD + 1 + i)
						.setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 4, eastUD.charAt(i));
				lines.get(vehicleNonCrossingSizeUD + 1 + i)
						.setCharAt((vehicleNonCrossingSizeLR + vehicleCrossingSizeLR) - 2, eastDU.charAt(i));

			}
		}

		for (int i = 0; i < linesCount; ++i) {
			System.out.println(lines.get(i).toString());
		}

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < ((vehicleNonCrossingSizeLR * 2) + vehicleCrossingSizeLR); ++j) {
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
