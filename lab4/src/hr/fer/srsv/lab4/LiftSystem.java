package hr.fer.srsv.lab4;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import hr.fer.srsv.lab4.enums.Direction;
import hr.fer.srsv.lab4.floor.Floor;
import hr.fer.srsv.lab4.lift.Lift;
import hr.fer.srsv.lab4.lift.Request;
import hr.fer.srsv.lab4.lift.RequestDisposer;
import hr.fer.srsv.lab4.traveler.Traveler;
import hr.fer.srsv.lab4.traveler.TravelerFactory;

public class LiftSystem {

	private final Random random;

	private final int interval;
	private final int newTravelerChance;
	private final int floorQuantity;
	private final int floorCapacity;
	private final List<Lift> lifts;
	private final List<Floor> floors;
	private final Queue<Traveler> travelers;

	private static RequestDisposer requestDisposer;

	public LiftSystem(final int interval, final int newTravelerChance, final int floorQuantity, final int floorCapacity,
			final List<Floor> floors, final List<Lift> lifts) {
		this.interval = interval;
		random = new Random();
		this.newTravelerChance = newTravelerChance;
		this.floorQuantity = floorQuantity;
		this.floorCapacity = floorCapacity;
		this.floors = floors;
		travelers = new LinkedList<>();

		this.lifts = lifts;
		requestDisposer = new RequestDisposer(lifts);
	}

	public static RequestDisposer getDisposer() {
		return requestDisposer;
	}

	public void run() {
		while (true) {
			manageLifts();
			manageTravelers();
			print();
			sleep();
		}
	}

	private void manageTravelers() {
		if (lucky(newTravelerChance)) {
			Traveler newTraveler = TravelerFactory.getInstance().newTraveler(floorQuantity);
			boolean added = floors.get(newTraveler.getSourceFloor()).addTraveler(newTraveler);
			if (!added) {
				TravelerFactory.getInstance().removeTraveler(newTraveler);
			} else {
				newTraveler.sendRequest();
				travelers.add(newTraveler);
			}
		}
	}

	private void manageLifts() {
		for (Lift lift : lifts) {
			if (lift.getDirection().equals(Direction.NONE)) {
				lift.checkAndSetNewDirection();
			} else {
				if (lift.isMoving()) {
					if (lift.atSemiFloor()) {
						lift.move();
					} else {
						if (lift.shouldStop()) {
							lift.stop();
						} else {
							lift.move();
						}
					}
				} else {
					if (lift.areDoorClosed()) {
						if (lift.shouldOpenDoor()) {
							lift.openDoor();
						} else {
							if (lift.isFull()) {
								lift.start();
							} else {
								if (lift.hasFurtherRequests()) {
									lift.start();
								} else {
									if (lift.hasBehindRequests()) {
										lift.changeDirection();
									}
								}
							}
						}
					} else {
						Floor floor = lift.getCurrentFloor();
						if (lift.hasRequestsForOut()) {
							floor.getNewArrivedTraveler(lift.freeTraveler());
						} else {
							if (lift.isFull()) {
								lift.closeDoor();
							} else {
								if (floor.hasTravelersForLiftAndDirection(lift)) {
									lift.addTraveler(floor.getWaitingTraveler(lift));
								} else {
									if (lift.hasFurtherRequests()) {
										lift.closeDoor();
									} else {
										if (lift.hasBehindRequests()) {
											lift.changeDirection();
										} else {
											lift.closeDoor();
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void print() {
		StringBuilder sb = new StringBuilder();
		sb.append(space(2 + floorCapacity + 1));
		int count = 0;
		for (Lift lift : lifts) {
			++count;
			int firstLine = ((lift.getCapacity() - 4) / 2) + 1;

			sb.append(space(firstLine));
			sb.append("Lift");
			sb.append(count);
			firstLine = ((lift.getCapacity() - 4) / 2);
			sb.append(space(firstLine));

		}
		System.out.println(sb.toString());

		sb = new StringBuilder();
		sb.append("Smjer/vrata: ");
		for (Lift lift : lifts) {
			int empty = ((lift.getCapacity() - 2) / 2) + 1;

			sb.append(space(empty));
			sb.append(lift.getDirection().name().substring(0, 1));
			sb.append(" ");
			sb.append(lift.getDoorStatus().name().substring(0, 1));
			empty = ((lift.getCapacity() - 2) / 2);
			sb.append(space(empty));
//			sb.append(" ");
//			sb.append(lift.isMoving());

		}
		System.out.println(sb.toString());

		sb = new StringBuilder();
		sb.append("Stajanja:");
		sb.append(space(((2 + floorCapacity) - 9) + 1));
		for (Lift lift : lifts) {
			int empty = ((lift.getCapacity() - 2) / 2);

			sb.append(space(empty));
			List<Request> requests = lift.getInnerRequests();
			int[] stops = new int[floorQuantity];
			for (int i = 0; i < stops.length; ++i) {
				stops[i] = 0;
			}
			for (Request request : requests) {
				++stops[request.getFloor().intValue()];
			}
			for (int i = 0; i < stops.length; ++i) {
				if (stops[i] > 0) {
					sb.append(stops[i]);
				} else {
					sb.append("-");
				}
			}
			empty = ((lift.getCapacity() - 2) / 2);

			sb.append(space(empty));
		}
		System.out.println(sb.toString());

		for (int i = (2 * floorQuantity) - 2; i >= 0; --i) {
			Floor floor = floors.get(i / 2);

			sb = new StringBuilder();
			if ((i % 2) == 0) {
				sb.append(i / 2);
				sb.append(":");
				for (Traveler traveler : floor.getWaitingTravelers()) {
					sb.append(traveler.getId());
				}
				sb.append(space(floorCapacity - floor.getWaitingTravelers().size()));
			} else {
				sb.append(space(2));
				for (int j = 0; j < floorCapacity; ++j) {
					sb.append("=");
				}
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
			if ((i % 2) == 0) {
				for (Traveler traveler : floor.getArrivedTravelers()) {
					sb.append(traveler.getId());
				}

			}
			System.out.println(sb.toString());
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

		sb = new StringBuilder();
		for (Traveler traveler : travelers) {
			sb.append(traveler.getId());
		}
		System.out.println(sb.toString());

		sb = new StringBuilder();
		for (Traveler traveler : travelers) {
			sb.append(traveler.getSourceFloor());
		}
		System.out.println(sb.toString());

		sb = new StringBuilder();
		for (Traveler traveler : travelers) {
			sb.append(traveler.getDestinationFloor());
		}
		System.out.println(sb.toString());

		sb = new StringBuilder();
		for (Traveler traveler : travelers) {
			Lift liftForTraveler = traveler.getLift();
			int liftIndex = 0;
			for (Lift lift : lifts) {
				++liftIndex;
				if (lift.equals(liftForTraveler)) {
					sb.append(liftIndex);
					break;
				}
			}
		}
		System.out.println(sb.toString());
		System.out.println();
		System.out.println("#####################################################################");
	}

	private void sleep() {
		try {
			Thread.sleep(interval);
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
