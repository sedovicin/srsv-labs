package hr.fer.srsv.lab4.lift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
//		for (int i = 0; i < lifts.size(); ++i) {
//			System.out.println("Lift" + i + " closeness:"
//					+ getLiftCloseness(lifts.get(i), reqSourceFloor, reqDestinationdirection));
//		}
		List<Lift> liftsSortedByCloseness = new ArrayList<>(lifts);
		Collections.sort(liftsSortedByCloseness, new CompareLiftCloseness(reqSourceFloor, reqDestinationdirection));
		return liftsSortedByCloseness.get(0);
	}

	private class CompareLiftCloseness implements Comparator<Lift> {

		private final Integer reqSourceFloor;
		private final Direction reqDestinationdirection;

		public CompareLiftCloseness(final Integer reqSourceFloor, final Direction reqDestinationdirection) {
			super();
			this.reqSourceFloor = reqSourceFloor;
			this.reqDestinationdirection = reqDestinationdirection;
		}

		@Override
		public int compare(final Lift o1, final Lift o2) {
			int o1Closeness = getLiftCloseness(o1, reqSourceFloor, reqDestinationdirection);
			int o2Closeness = getLiftCloseness(o2, reqSourceFloor, reqDestinationdirection);

			if (Math.abs(o1Closeness - o2Closeness) < 2) {
				return Integer.compare(o1.getCapacity(), o2.getCapacity());
			}
			return Integer.compare(o1Closeness, o2Closeness);

		}

		private boolean liftFurtherAndWrongDirection(final Lift lift, final Integer reqSourceFloor,
				final Direction reqDestinationdirection) {
			int further = Integer.compare(lift.getCurrentFloorIndex(), reqSourceFloor);
			return ((further > 0) && lift.getDirection().equals(Direction.UP))
					|| ((further < 0) && lift.getDirection().equals(Direction.DOWN));
		}

		private boolean liftHasToPassFurtherOfReq(final Lift lift, final Integer reqSourceFloor,
				final Direction reqDestinationdirection) {
			int further = Integer.compare(lift.getCurrentFloorIndex(), reqSourceFloor);
			return ((further >= 0) && lift.getDirection().equals(Direction.DOWN)
					&& reqDestinationdirection.equals(Direction.UP) && lift.hasFurtherRequests(reqSourceFloor))
					|| ((further <= 0) && lift.getDirection().equals(Direction.UP)
							&& reqDestinationdirection.equals(Direction.DOWN)
							&& lift.hasFurtherRequests(reqSourceFloor));

		}

		private int getLiftCloseness(final Lift lift, final Integer reqSourceFloor,
				final Direction reqDestinationdirection) {
			if (lift.hasAnyRequests() && (liftFurtherAndWrongDirection(lift, reqSourceFloor, reqDestinationdirection)
					|| liftHasToPassFurtherOfReq(lift, reqSourceFloor, reqDestinationdirection))) {
				return Integer.MAX_VALUE;
			} else {
				return Math.abs(lift.getCurrentFloorIndex() - reqSourceFloor);

			}
		}

	}
}
