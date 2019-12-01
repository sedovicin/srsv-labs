package hr.fer.srsv.lab3.traveler;

import hr.fer.srsv.lab3.lift.Lift;

public class Traveler {

	private String id;
	private Integer sourceLocation;
	private Integer destinationLocation;

	private Lift lift;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Integer getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(final Integer sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public Integer getDestinationLocation() {
		return destinationLocation;
	}

	public void setDestinationLocation(final Integer destinationLocation) {
		this.destinationLocation = destinationLocation;
	}

	public Lift getLift() {
		return lift;
	}

	public void setLift(final Lift lift) {
		this.lift = lift;
	}

	public Traveler(final String id, final Integer sourceLocation, final Integer destinationLocation) {
		super();
		this.id = id;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
	}

}
