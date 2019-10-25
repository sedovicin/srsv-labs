package hr.fer.srsv.movables;

public abstract class Movable {

	protected final String id;
	protected final int speed;
	protected int stepsUntilNextMove;
	protected final int length;

	public Movable(final String id, final int speed, final int length) {
		this.id = id;
		this.speed = speed;
		stepsUntilNextMove = speed;
		this.length = length;
	}

	@Override
	public String toString() {
		return id;
	}

	public int getLength() {
		return length;
	}

	public int getSpeed() {
		return speed;
	}

	/**
	 * Informs movable that it should move.
	 *
	 * @return true if enough "pushes" occured so the movable should move one step,
	 *         false otherwise.
	 */
	public boolean move() {
		if (--stepsUntilNextMove <= 0) {
			stepsUntilNextMove = speed;
			return true;
		}
		return false;
	}

	/**
	 * Informs movable that it cannot move, so it stops
	 */
	public void stop() {
		stepsUntilNextMove = speed;
	}

}