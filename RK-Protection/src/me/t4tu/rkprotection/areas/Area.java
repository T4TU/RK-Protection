package me.t4tu.rkprotection.areas;

import java.util.List;

import org.bukkit.Location;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkprotection.Protection;

public class Area {
	
	private String name;
	private int id;
	private List<SubArea> subAreas;
	private List<Flag> flags;
	private Location respawnLocation;
	
	public Area(String name, int id, List<SubArea> subAreas, List<Flag> flags) {
		this.name = name;
		this.id = id;
		this.subAreas = subAreas;
		this.flags = flags;
	}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public List<SubArea> getSubAreas() {
		return subAreas;
	}
	
	public SubArea getSubAreaByName(String name) {
		for (SubArea subArea : subAreas) {
			if (subArea.getName().equalsIgnoreCase(name)) {
				return subArea;
			}
		}
		return null;
	}
	
	public List<Flag> getFlags() {
		return flags;
	}
	
	public boolean hasFlag(Flag flag) {
		return flags.contains(flag);
	}
	
	public Location getRespawnLocation() {
		return respawnLocation;
	}
	
	public void setRespawnLocation(Location respawnLocation) {
		this.respawnLocation = respawnLocation;
	}
	
	public void addSubArea(SubArea subArea) {
		CoreUtils.setLocation(Protection.getPlugin(), "areas." + id + ".sub-areas." + subArea.getName() + ".corner-1", subArea.getLocation1());
		CoreUtils.setLocation(Protection.getPlugin(), "areas." + id + ".sub-areas." + subArea.getName() + ".corner-2", subArea.getLocation2());
		reloadSubAreas();
	}
	
	public void removeSubArea(String name) {
		Protection.getPlugin().getConfig().set("areas." + id + ".sub-areas." + name, null);
		Protection.getPlugin().saveConfig();
		reloadSubAreas();
	}
	
	public void reloadSubAreas() {
		subAreas.clear();
		if (Protection.getPlugin().getConfig().getConfigurationSection("areas." + id + ".sub-areas") != null) {
			for (String s2 : Protection.getPlugin().getConfig().getConfigurationSection("areas." + id + ".sub-areas").getKeys(false)) {
				Location location1 = CoreUtils.loadLocation(Protection.getPlugin(), "areas." + id + ".sub-areas." + s2 + ".corner-1");
				Location location2 = CoreUtils.loadLocation(Protection.getPlugin(), "areas." + id + ".sub-areas." + s2 + ".corner-2");
				SubArea subArea = new SubArea(s2, location1, location2);
				subAreas.add(subArea);
			}
		}
	}
	
	public void addFlag(Flag flag) {
		List<String> flags = Protection.getPlugin().getConfig().getStringList("areas." + id + ".flags");
		if (!flags.contains(flag.toString())) {
			flags.add(flag.toString());
		}
		Protection.getPlugin().getConfig().set("areas." + id + ".flags", flags);
		Protection.getPlugin().saveConfig();
		reloadFlags();
	}
	
	public void removeFlag(Flag flag) {
		List<String> flags = Protection.getPlugin().getConfig().getStringList("areas." + id + ".flags");
		flags.remove(flag.toString());
		Protection.getPlugin().getConfig().set("areas." + id + ".flags", flags);
		Protection.getPlugin().saveConfig();
		reloadFlags();
	}
	
	public void reloadFlags() {
		flags.clear();
		try {
			for (String flag : Protection.getPlugin().getConfig().getStringList("areas." + id + ".flags")) {
				flags.add(Flag.valueOf(flag));
			}
		}
		catch (Exception e) { }
	}
}