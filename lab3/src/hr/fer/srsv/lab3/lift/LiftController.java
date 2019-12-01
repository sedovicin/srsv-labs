package hr.fer.srsv.lab3.lift;

public class LiftController {

	private static LiftController liftController;

	private int floorAmount;

	public int getFloors() {
		return floorAmount;
	}

	public static LiftController getInstance() {
		if (liftController == null) {
			liftController = new LiftController();
		}
		return liftController;
	}
}
