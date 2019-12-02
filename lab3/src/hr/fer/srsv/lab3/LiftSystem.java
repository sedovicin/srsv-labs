package hr.fer.srsv.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hr.fer.srsv.lab3.floor.Floor;
import hr.fer.srsv.lab3.traveler.Traveler;
import hr.fer.srsv.lab3.traveler.TravelerFactory;

public class LiftSystem {

	private final Random random;

	private final int newTravelerChance;
	private final int floorQuantity;
	private final List<Floor> floors;

	public LiftSystem(final int newTravelerChance, final int floorQuantity, final int floorCapacity) {
		random = new Random();
		this.newTravelerChance = newTravelerChance;
		this.floorQuantity = floorQuantity;
		floors = new ArrayList<>(floorQuantity);
		for (int i = 0; i < floorQuantity; ++i) {
			floors.add(new Floor(i, floorCapacity));
		}
	}

	public void run() {
		while (true) {
			manageTravelers();
			manageLifts();
			print();
			sleep();
		}
	}

	private void manageTravelers() {
		if (lucky(newTravelerChance)) {
			Traveler newTraveler = TravelerFactory.getInstance().newTraveler(floorQuantity);
			boolean added = floors.get(newTraveler.getSourceLocation()).addTraveler(newTraveler);
			if (!added) {
				TravelerFactory.getInstance().removeTraveler(newTraveler);
			}
		}
	}

	private void manageLifts() {
		// TODO Auto-generated method stub

	}

	private void print() {
		// TODO Auto-generated method stub

	}

	private void sleep() {
		// TODO Auto-generated method stub

	}

	private boolean lucky(final int chancePercentage) {
		if (random.nextInt(100) < chancePercentage) {
			return true;
		}
		return false;
	}
}
