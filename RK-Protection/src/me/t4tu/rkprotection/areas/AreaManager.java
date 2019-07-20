package me.t4tu.rkprotection.areas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.t4tu.rkcore.utils.CoreUtils;
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
	
	public Area getAreaById(int id) {
		for (Area area : areas) {
			if (area.getId() == id) {
				return area;
			}
		}
		return null;
	}
	
	public Area getAreaById(String id) {
		for (Area area : areas) {
			if (id.equalsIgnoreCase("" + area.getId())) {
				return area;
			}
		}
		return null;
	}
	
	public Area getArea(Location location) {
		Area toBeReturned = null;
		Location l = location.getBlock().getLocation();
		for (Area area : areas) {
			for (SubArea subArea : area.getSubAreas()) {
				Location location1 = subArea.getLocation1();
				Location location2 = subArea.getLocation2();
				if (location1 == null || location2 == null) {
					continue;
				}
				if (l.getWorld().getName().equals(location1.getWorld().getName()) && l.getWorld().getName().equals(location2.getWorld().getName())) {
					if (((location1.getX() <= l.getX()) && (l.getX() <= location2.getX())) || 
							((location1.getX() >= l.getX()) && (l.getX() >= location2.getX()))) {
						if (((location1.getY() <= l.getY()) && (l.getY() <= location2.getY())) || 
								((location1.getY() >= l.getY()) && (l.getY() >= location2.getY()))) {
							if (((location1.getZ() <= l.getZ()) && (l.getZ() <= location2.getZ())) || 
									((location1.getZ() >= l.getZ()) && (l.getZ() >= location2.getZ()))) {
								if (toBeReturned != null) {
									if (area.getId() > toBeReturned.getId()) {
										toBeReturned = area;
									}
								}
								else {
									toBeReturned = area;
								}
							}
						}
					}
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
					List<SubArea> subAreas = new ArrayList<SubArea>();
					if (Protection.getPlugin().getConfig().getConfigurationSection("areas." + s + ".sub-areas") != null) {
						for (String s2 : Protection.getPlugin().getConfig().getConfigurationSection("areas." + s + ".sub-areas").getKeys(false)) {
							Location location1 = CoreUtils.loadLocation(Protection.getPlugin(), "areas." + s + ".sub-areas." + s2 + ".corner-1");
							Location location2 = CoreUtils.loadLocation(Protection.getPlugin(), "areas." + s + ".sub-areas." + s2 + ".corner-2");
							SubArea subArea = new SubArea(s2, location1, location2);
							subAreas.add(subArea);
						}
					}
					List<Flag> flags = new ArrayList<Flag>();
					for (String flag : Protection.getPlugin().getConfig().getStringList("areas." + s + ".flags")) {
						flags.add(Flag.valueOf(flag));
					}
					Area area = new Area(name, id, subAreas, flags);
					area.setRespawnLocation(CoreUtils.loadLocation(Protection.getPlugin(), "areas." + s + ".respawn-location"));
					areas.add(area);
				}
				catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("Virhe ladattaessa aluetta ID:llä '" + s + "'");
				}
			}
		}
	}
	
	public List<String> getPvPBypasses() {
		return pvpbypass;
	}
}