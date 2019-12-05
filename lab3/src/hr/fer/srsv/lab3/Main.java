package hr.fer.srsv.lab3;

import java.util.ArrayList;
import java.util.List;

import hr.fer.srsv.lab3.floor.Floor;
import hr.fer.srsv.lab3.lift.Lift;

public class Main {

	public static void main(final String[] args) {
		int interval = 500;
		int floorQuantity = 4;
		int floorCapacity = 10;
		int liftCapacity = 6;
		int newTravelerChance = 20;

		List<Floor> floors = new ArrayList<>();
		for (int i = 0; i < floorQuantity; ++i) {
			floors.add(new Floor(floorCapacity));
		}
		List<Lift> lifts = new ArrayList<>();
		lifts.add(new Lift(liftCapacity, floors));
		new LiftSystem(interval, newTravelerChance, floorQuantity, floorCapacity, floors, lifts).run();
	}
}
