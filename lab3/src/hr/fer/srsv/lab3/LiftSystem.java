package hr.fer.srsv.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hr.fer.srsv.lab3.enums.Direction;
import hr.fer.srsv.lab3.floor.Floor;
import hr.fer.srsv.lab3.lift.Lift;
import hr.fer.srsv.lab3.lift.Request;
import hr.fer.srsv.lab3.traveler.Traveler;
import hr.fer.srsv.lab3.traveler.TravelerFactory;

public class LiftSystem {

	private final Random random;

	private final int newTravelerChance;
	private final int floorQuantity;
	private final int floorCapacity;
	private final List<Lift> lifts;
	private final List<Floor> floors;

	public LiftSystem(final int newTravelerChance, final int floorQuantity, final int floorCapacity,
			final List<Lift> lifts) {
		random = new Random();
		this.newTravelerChance = newTravelerChance;
		this.floorQuantity = floorQuantity;
		this.floorCapacity = floorCapacity;
		floors = new ArrayList<>(floorQuantity);
		for (int i = 0; i < floorQuantity; ++i) {
			floors.add(new Floor(i, floorCapacity));
		}
		this.lifts = lifts;
	}

	public void run() {
		while (true) {

//			manageLifts();
			print();
//			manageTravelers();
			sleep();
		}
	}

	private void manageTravelers() {
		if (lucky(newTravelerChance)) {
			Traveler newTraveler = TravelerFactory.getInstance().newTraveler(floorQuantity);
			boolean added = floors.get(newTraveler.getSourceLocation()).addTraveler(newTraveler);
			if (!added) {
				TravelerFactory.getInstance().removeTraveler(newTraveler);
			} else {
				newTraveler.sendRequest();
			}
		}
	}

	private void manageLifts() {
		for (Lift lift : lifts) {
			if (lift.getDirection().equals(Direction.NONE)) {
				if (!(lift.hasRequests())) {
					lift.stoppedAndDoorClosed();
				} else if (lift.hasNewRequests()) {
					lift.handleNewRequest();
					lift.setDirection();
				}
			} else {
				if (lift.reachedDestination()) {
					lift.stoppedAndDoorClosed();
				} else {
					lift.move();
				}
			}

		}

	}

	private void print() {
		int firstLine = 2 + floorCapacity + 2 + ((lifts.get(0).getCapacity() - 5) / 2);
		StringBuilder sb = new StringBuilder();
		sb.append(space(firstLine));
		sb.append("Lift1");
		System.out.println(sb.toString());

		sb = new StringBuilder();
		sb.append("Stajanja:");
		sb.append(space(((2 + floorCapacity) - 9) + 1));
		Lift firstLift = lifts.get(0);
		List<Request> requests = firstLift.getHandlingRequests();
		boolean[] stops = new boolean[floorQuantity];
		for (int i = 0; i < stops.length; ++i) {
			stops[i] = false;
		}
		for (Request request : requests) {
			if (request.getFloorSource() != null) {
				stops[request.getFloorSource().intValue()] = true;
			} else if (request.getFloorDestination() != null) {
				stops[request.getFloorSource().intValue()] = true;
			}
		}
		for (int i = 0; i < stops.length; ++i) {
			if (stops[i] == true) {
				sb.append("*");
			} else {
				sb.append("-");
			}
		}
		System.out.println(sb.toString());

		for (int i = floorQuantity - 1; i >= 0; --i) {
			Floor floor = floors.get(i);
			sb = new StringBuilder();
			sb.append(i);
			sb.append(":");
			for (Traveler traveler : floor.getWaitingTravelers()) {
				sb.append(traveler.getId());
			}
			sb.append(space(floorCapacity - floor.getWaitingTravelers().size()));

			sb.append("|");
			for (Lift lift : lifts) {
				if (lift.getPosition() == i) {
					sb.append("[");
					for (Traveler traveler : lift.getTravelers()) {
						sb.append(traveler.getId());
					}
					sb.append(space(lift.getCapacity() - lift.getTravelers().size()));
					sb.append("]");
				} else {
					sb.append(space(2 + lift.getCapacity()));
				}
				sb.append("|");
			}
			System.out.println(sb.toString());

			if (i > 0) {
				sb = new StringBuilder();
				sb.append(space(2));
				for (int j = 0; j < floorCapacity; ++j) {
					sb.append("=");
				}
				sb.append("|");
				for (Lift lift : lifts) {
					if (lift.getPosition() == i) {
						sb.append("[");
						for (Traveler traveler : lift.getTravelers()) {
							sb.append(traveler.getId());
						}
						sb.append(space(lift.getCapacity() - lift.getTravelers().size()));
						sb.append("]");
					} else {
						sb.append(space(2 + lift.getCapacity()));
					}
					sb.append("|");
				}
				System.out.println(sb.toString());
			}
		}

		sb = new StringBuilder();
		int groundSize = 2 + floorCapacity + 1 + 1;
		for (Lift lift : lifts) {
			groundSize += 2 + lift.getCapacity();
		}
		for (int i = 0; i < groundSize; ++i) {
			sb.append("=");
		}
		System.out.println(sb.toString());
	}

	private void sleep() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private boolean lucky(final int chancePercentage) {
		if (random.nextInt(100) < chancePercentage) {
			return true;
		}
		return false;
	}

	private String space(final int quantity) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < quantity; ++i) {
			sb.append(" ");
		}
		return sb.toString();
	}
}
