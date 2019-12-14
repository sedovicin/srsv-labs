package hr.fer.srsv.lab4.floor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import hr.fer.srsv.lab4.lift.Lift;
import hr.fer.srsv.lab4.traveler.Traveler;

public class Floor {
	private final Queue<Traveler> waitingTravelers;
	private final Queue<Traveler> arrivedTravelers;

	public Floor(final int capacity) {
		waitingTravelers = new LinkedBlockingQueue<>(capacity);
		arrivedTravelers = new LinkedList<>();
	}

	public boolean addTraveler(final Traveler traveler) {
		return waitingTravelers.offer(traveler);
	}

	public Queue<Traveler> getWaitingTravelers() {
		return waitingTravelers;
	}

	public Queue<Traveler> getArrivedTravelers() {
		return arrivedTravelers;
	}

	public void getNewArrivedTraveler(final Traveler traveler) {
		if (traveler != null) {
			arrivedTravelers.add(traveler);
		}
	}

	public Traveler getWaitingTraveler(final Lift lift) {
		for (Traveler traveler : waitingTravelers) {
			if ((traveler.getLift() == lift) && traveler.getDirection().equals(lift.getDirection())) {
				waitingTravelers.remove(traveler);
				return traveler;
			}
		}
		return null;
	}

	public boolean hasTravelersForLiftAndDirection(final Lift lift) {
		for (Traveler traveler : waitingTravelers) {
			if ((traveler.getLift() == lift) && traveler.getDirection().equals(lift.getDirection())) {
				return true;
			}
		}
		return false;
	}
}
