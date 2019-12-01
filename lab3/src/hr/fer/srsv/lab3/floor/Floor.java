package hr.fer.srsv.lab3.floor;

import java.util.List;

import hr.fer.srsv.lab3.traveler.Traveler;

public class Floor {

	private Integer level;
	private Integer capacity;
	private List<Traveler> waitingTravelers;
	private List<Traveler> arrivedTravelers;

	public Integer getLevel() {
		return level;
	}

	public void setLevel(final Integer level) {
		this.level = level;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(final Integer capacity) {
		this.capacity = capacity;
	}

	public List<Traveler> getWaitingTravelers() {
		return waitingTravelers;
	}

	public void setWaitingTravelers(final List<Traveler> waitingTravelers) {
		this.waitingTravelers = waitingTravelers;
	}

	public List<Traveler> getArrivedTravelers() {
		return arrivedTravelers;
	}

	public void setArrivedTravelers(final List<Traveler> arrivedTravelers) {
		this.arrivedTravelers = arrivedTravelers;
	}

}
