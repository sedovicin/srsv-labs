package hr.fer.srsv.enums;

public enum Direction {
	UP_TO_DOWN('v'), DOWN_TO_UP('^'), LEFT_TO_RIGHT('>'), RIGHT_TO_LEFT('<');

	private Character value;

	private Direction(final Character value) {
		this.value = value;
	}

	public Character getValue() {
		return value;
	}
}
