package uk.ac.horizon.artcodes.model;


public class Location {
	final String type;
	final String name;
	final String address;
	final Double[] coordinates;

	public Location(String name, String address, Double[] coordinates) {
		this.type = "Point";
		this.name = name;
		this.address = address;
		this.coordinates = coordinates;
	}
}
