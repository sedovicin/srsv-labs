package hr.fer.srsv.lab3.floor;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import hr.fer.srsv.lab3.traveler.Traveler;

public class Floor {

	private final Integer level;
	private final Integer capacity;
	private final Queue<Traveler> waitingTravelers;
	private final Queue<Traveler> arrivedTravelers;

	public Floor(final int level, final int capacity) {
		this.level = level;
		this.capacity = capacity;
		waitingTravelers = new LinkedBlockingQueue<>(capacity);
		arrivedTravelers = new LinkedBlockingQueue<>(capacity);
	}

	public boolean addTraveler(final Traveler traveler) {
		return waitingTravelers.offer(traveler);
	}
}
