package hr.fer.srsv.lab4.traveler;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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

	public Traveler newTraveler(final int floorQuantity) {
		char newId = nextId();
		activeIds.add(Integer.valueOf(newId));

		Integer sourceFloor = random.nextInt(floorQuantity);
		Integer destinationFloor = destinationFloor(floorQuantity, sourceFloor);

		return new Traveler(String.valueOf(integerToLetter(newId)), sourceFloor, destinationFloor);
	}

	public void removeTraveler(final Traveler traveler) {
		int id = traveler.getId().charAt(0);

		id = letterToInteger(id);
		activeIds.remove(id);
	}

	private char nextId() {
		int nextInt = random.nextInt(52);
		while (activeIds.contains(Integer.valueOf(nextInt))) {
			nextInt = random.nextInt(52);
		}
		return (char) nextInt;
	}

	private char integerToLetter(final char random) {
		char newNumber = (char) (random + 'A');
		if ((newNumber > 'Z') && (newNumber < 'a')) {
			newNumber += 6;
		}
		return newNumber;
	}

	private Integer destinationFloor(final int floorQuantity, final Integer sourceFloor) {
		Integer destinationFloor = random.nextInt(floorQuantity);
		while (sourceFloor.equals(destinationFloor)) {
			destinationFloor = random.nextInt(floorQuantity);
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
