package me.t4tu.rkprotection.areas;

import org.bukkit.Location;

public class SubArea {
	
	private String name;
	private Location location1;
	private Location location2;
	
	public SubArea(String name, Location location1, Location location2) {
		this.name = name;
		this.location1 = location1.getBlock().getLocation();
		this.location2 = location2.getBlock().getLocation();
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLocation1() {
		return location1;
	}
	
	public Location getLocation2() {
		return location2;
	}
}