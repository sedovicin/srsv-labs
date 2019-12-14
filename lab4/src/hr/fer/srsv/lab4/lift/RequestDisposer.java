package hr.fer.srsv.lab4.lift;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import hr.fer.srsv.lab4.enums.Direction;
import hr.fer.srsv.lab4.enums.RequestType;

public class RequestDisposer {
	private final List<Lift> lifts;

	public RequestDisposer(final List<Lift> lifts) {
		super();
		this.lifts = lifts;
	}

	public Lift acknowledgeFloorRequest(final Integer reqSourceFloor, final Direction reqDestinationdirection) {
		Lift liftForRequest = chooseLiftForRequest(reqSourceFloor, reqDestinationdirection);
		liftForRequest.addFloorRequest(new Request(RequestType.FLOOR, reqSourceFloor, reqDestinationdirection));
		return liftForRequest;
	}

	private Lift chooseLiftForRequest(final Integer reqSourceFloor, final Direction reqDestinationdirection) {

		return getClosestLift(reqSourceFloor, reqDestinationdirection);
	}

	private Lift getClosestLift(final Integer reqSourceFloor, final Direction reqDestinationdirection) {
		Map<Integer, Set<Lift>> liftCloseness = new TreeMap<>();
		for (Lift lift : lifts) {
			if (liftFurtherAndWrongDirection(lift, reqSourceFloor, reqDestinationdirection)
					|| liftHasToPassFurtherOfReq(lift, reqSourceFloor, reqDestinationdirection)) {
				Set<Lift> furthestLifts = liftCloseness.get(Integer.MAX_VALUE);
				if (furthestLifts == null) {
					furthestLifts = new TreeSet<>();
					liftCloseness.put(Integer.MAX_VALUE, furthestLifts);
				}
				furthestLifts.add(lift);
			} else {
				int closeness = Math.abs(lift.getCurrentFloorIndex() - reqSourceFloor);
				Set<Lift> furthestLifts = liftCloseness.get(closeness);
				if (furthestLifts == null) {
					furthestLifts = new TreeSet<>();
					liftCloseness.put(closeness, furthestLifts);
				}
				furthestLifts.add(lift);
			}
		}

		Set<Lift> closestLifts = liftCloseness
				.get(liftCloseness.keySet().toArray(new Integer[liftCloseness.keySet().size()])[0]);

		return closestLifts.toArray(new Lift[liftCloseness.keySet().size()])[0];
	}

	private boolean liftHasToPassFurtherOfReq(final Lift lift, final Integer reqSourceFloor,
			final Direction reqDestinationdirection) {
		int further = Integer.compare(lift.getCurrentFloorIndex(), reqSourceFloor);
		return ((further > 0) && lift.getDirection().equals(Direction.DOWN)
				&& reqDestinationdirection.equals(Direction.UP) && lift.hasFurtherRequests(reqSourceFloor))
				|| ((further > 0) && lift.getDirection().equals(Direction.UP)
						&& reqDestinationdirection.equals(Direction.DOWN) && lift.hasFurtherRequests(reqSourceFloor));

	}

	private boolean liftFurtherAndWrongDirection(final Lift lift, final Integer reqSourceFloor,
			final Direction reqDestinationdirection) {
		int further = Integer.compare(lift.getCurrentFloorIndex(), reqSourceFloor);
		return ((further > 0) && lift.getDirection().equals(Direction.UP)
				&& reqDestinationdirection.equals(Direction.DOWN))
				|| ((further < 0) && lift.getDirection().equals(Direction.DOWN)
						&& reqDestinationdirection.equals(Direction.UP));
	}
}
