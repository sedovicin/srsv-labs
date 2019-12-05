package hr.fer.srsv.lab3.lift;

import java.util.List;

import hr.fer.srsv.lab3.enums.Direction;
import hr.fer.srsv.lab3.enums.RequestType;

public class RequestDisposer {
	private final List<Lift> lifts;

	public RequestDisposer(final List<Lift> lifts) {
		super();
		this.lifts = lifts;
	}

	public Lift acknowledgeFloorRequest(final Integer reqSourceFloor, final Direction reqDestinationdirection) {
		Lift liftForRequest = chooseLiftForRequest(reqSourceFloor);
		liftForRequest.addFloorRequest(new Request(RequestType.FLOOR, reqSourceFloor, reqDestinationdirection));
		return liftForRequest;
	}

	private Lift chooseLiftForRequest(final Integer reqSourceFloor) {
		return lifts.get(0);
	}
}
