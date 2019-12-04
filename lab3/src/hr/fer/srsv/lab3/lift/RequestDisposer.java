package hr.fer.srsv.lab3.lift;

import java.util.List;

import hr.fer.srsv.lab3.enums.Direction;

public class RequestDisposer {

	private final List<Lift> lifts;

	public RequestDisposer(final List<Lift> lifts) {
		super();
		this.lifts = lifts;
	}

	public void acknowledge(final Integer reqSourceFloor, final Direction reqDestinationdirection) {
		Lift liftForRequest = chooseLiftForRequest(reqSourceFloor);
		liftForRequest.add(new Request(Request.Type.FLOOR, reqSourceFloor, reqDestinationdirection));
	}

	private Lift chooseLiftForRequest(final Integer reqSourceFloor) {
		return lifts.get(0);
	}
}
