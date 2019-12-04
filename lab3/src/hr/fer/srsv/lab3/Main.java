package hr.fer.srsv.lab3;

import java.util.ArrayList;
import java.util.List;

import hr.fer.srsv.lab3.lift.Lift;

public class Main {

	public static void main(final String[] args) {
		List<Lift> lifts = new ArrayList<>();
		lifts.add(new Lift(6));
		new LiftSystem(10, 4, 10, lifts).run();
	}

}
