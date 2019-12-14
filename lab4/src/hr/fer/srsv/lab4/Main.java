package hr.fer.srsv.lab4;

import java.util.ArrayList;
import java.util.List;

import hr.fer.srsv.lab4.floor.Floor;
import hr.fer.srsv.lab4.lift.Lift;

public class Main {

	public static void main(final String[] args) {
		int interval = 500;
		int floorQuantity = 4;
		int floorCapacity = 10;
		int lift1Capacity = 10;
		int lift2Capacity = 6;
		int newTravelerChance = 20;

		List<Floor> floors = new ArrayList<>();
		for (int i = 0; i < floorQuantity; ++i) {
			floors.add(new Floor(floorCapacity));
		}
		List<Lift> lifts = new ArrayList<>();
		lifts.add(new Lift(lift1Capacity, floors));
		lifts.add(new Lift(lift2Capacity, floors));

		new LiftSystem(interval, newTravelerChance, floorQuantity, floorCapacity, floors, lifts).run();
	}
}
