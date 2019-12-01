package hr.fer.srsv.lab3.traveler;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import hr.fer.srsv.lab3.lift.LiftController;

public class TravelerFactory {

	private static TravelerFactory tf;
	private final Random random;
	private final List<Integer> activeIds;

	private TravelerFactory() {
		random = new Random();
		activeIds = new LinkedList<>();

	}

	public static TravelerFactory getInstance() {
		if (tf == null) {
			tf = new TravelerFactory();
		}
		return tf;
	}

	public Traveler newTraveler() {

		char newId = nextId();
		activeIds.add(Integer.valueOf(integerToLetter(newId)));

		Integer sourceFloor = random.nextInt(LiftController.getInstance().getFloors());
		Integer destinationFloor = destinationFloor(sourceFloor);

		return new Traveler(String.valueOf(newId), sourceFloor, destinationFloor);
	}

	public void removeTraveler(final Traveler traveler) {
		Integer id = Integer.parseInt(traveler.getId());

		id = letterToInteger(id);
		activeIds.remove(id);
	}

	private char nextId() {
		Integer nextInt = random.nextInt(52);
		while (activeIds.contains(nextInt)) {
			nextInt = random.nextInt(52);
		}
		return (char) nextInt.intValue();
	}

	private char integerToLetter(final char random) {
		char newNumber = (char) (random + 65);
		if ((newNumber > 90) && (newNumber < 97)) {
			newNumber += 6;
		}
		return newNumber;
	}

	private Integer destinationFloor(final Integer sourceFloor) {
		Integer destinationFloor = random.nextInt(LiftController.getInstance().getFloors());
		while (sourceFloor.equals(destinationFloor)) {
			destinationFloor = random.nextInt(LiftController.getInstance().getFloors());
		}
		return destinationFloor;
	}

	private Integer letterToInteger(final Integer letter) {
		int randomNumber = letter.intValue();
		if (letter >= 97) {
			randomNumber -= 6;
		}
		randomNumber -= 65;
		return Integer.valueOf(randomNumber);
	}
}
