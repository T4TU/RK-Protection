package me.t4tu.rkprotection.areas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.t4tu.rkprotection.Protection;

public class AreaManager {
	
	private ArrayList<Area> areas = new ArrayList<Area>();
	private ArrayList<String> pvpbypass = new ArrayList<String>();
	
	public ArrayList<Area> getAreas() {
		return areas;
	}
	
	public void removeArea(Area area) {
		areas.remove(area);
	}
	
	public Area getAreaByName(String name) {
		for (Area area : areas) {
			if (area.getName().equals(name)) {
				return area;
			}
		}
		return null;
	}
	
	public Area getAreaByID(int id) {
		for (Area area : areas) {
			if (area.getID() == id) {
				return area;
			}
		}
		return null;
	}
	
	public Area getAreaByID(String id) {
		for (Area area : areas) {
			if (id.equalsIgnoreCase("" + area.getID())) {
				return area;
			}
		}
		return null;
	}
	
	public Area getArea(Location location) {
		Area toBeReturned = null;
		for (Area area : areas) {
			if (location.getWorld().getName().equals(area.getWorld()) && location.getX() >= area.getX1() && location.getX() <= area.getX2() && 
					location.getY() >= area.getY1() && location.getY() <= area.getY2() && 
					location.getZ() >= area.getZ1() && location.getZ() <= area.getZ2()) {
				if (toBeReturned != null) {
					if (area.getID() > toBeReturned.getID()) {
						toBeReturned = area;
					}
				}
				else {
					toBeReturned = area;
				}
			}
		}
		return toBeReturned;
	}
	
	public void loadAreasFromConfig() {
		areas.clear();
		if (Protection.getPlugin().getConfig().getConfigurationSection("areas") != null) {
			for (String s : Protection.getPlugin().getConfig().getConfigurationSection("areas").getKeys(false)) {
				try {
					String name = Protection.getPlugin().getConfig().getString("areas." + s + ".name");
					int id = Integer.parseInt(s);
					String world = Protection.getPlugin().getConfig().getString("areas." + s + ".world");
					int x1 = Protection.getPlugin().getConfig().getInt("areas." + s + ".x1");
					int y1 = Protection.getPlugin().getConfig().getInt("areas." + s + ".y1");
					int z1 = Protection.getPlugin().getConfig().getInt("areas." + s + ".z1");
					int x2 = Protection.getPlugin().getConfig().getInt("areas." + s + ".x2");
					int y2 = Protection.getPlugin().getConfig().getInt("areas." + s + ".y2");
					int z2 = Protection.getPlugin().getConfig().getInt("areas." + s + ".z2");
					Area area = new Area(name, id, world, x1, y1, z1, x2, y2, z2);
					if (Protection.getPlugin().getConfig().getStringList("areas." + s + ".flags") != null) {
						for (String flag : Protection.getPlugin().getConfig().getStringList("areas." + s + ".flags")) {
							area.addFlag(Flag.valueOf(flag));
						}
					}
					areas.add(area);
				}
				catch (NumberFormatException e) {
					Bukkit.getConsoleSender().sendMessage("Virhe ladattaessa aluetta ID:llä '" + s + "'");
				}
			}
		}
	}
	
	public List<String> getPvPBypasses() {
		return pvpbypass;
	}
}