package me.t4tu.rkprotection.areas;

import java.util.ArrayList;
import java.util.List;

import me.t4tu.rkprotection.Protection;

public class Area {
	
	private ArrayList<Flag> flags = new ArrayList<Flag>();
	private String name;
	private int ID;
	private String world;
	private int x1;
	private int y1;
	private int z1;
	private int x2;
	private int y2;
	private int z2;
	
	public Area(String name, int ID, String world, int x1, int y1, int z1, int x2, int y2, int z2) {
		this.name = name;
		this.ID = ID;
		this.world = world;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
	}
	
	public ArrayList<Flag> getFlags() {
		return flags;
	}
	
	public boolean hasFlag(Flag flag) {
		return flags.contains(flag);
	}
	
	public void addFlag(Flag flag) {
		if (!flags.contains(flag)) {
			flags.add(flag);
			if (Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags") != null) {
				if (!Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags").contains(flag.toString())) {
					List<String> flags = Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags");
					flags.add(flag.toString());
					Protection.getPlugin().getConfig().set("areas." + ID + ".flags", flags);
					Protection.getPlugin().saveConfig();
				}
			}
			else {
				ArrayList<String> flags = new ArrayList<String>();
				flags.add(flag.toString());
				Protection.getPlugin().getConfig().set("areas." + ID + ".flags", flags);
				Protection.getPlugin().saveConfig();
			}
		}
	}
	
	public void removeFlag(Flag flag) {
		flags.remove(flag);
		if (Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags") != null) {
			List<String> flags = Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags");
			flags.remove(flag.toString());
			Protection.getPlugin().getConfig().set("areas." + ID + ".flags", flags);
			Protection.getPlugin().saveConfig();
		}
	}
	
	public void reloadFlags() {
		flags.clear();
		if (Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags") != null) {
			for (String flag : Protection.getPlugin().getConfig().getStringList("areas." + ID + ".flags")) {
				flags.add(Flag.valueOf(flag));
			}
		}
	}
	
	public String getWorld() {
		return world;
	}
	
	public int getX1() {
		return x1;
	}
	
	public int getY1() {
		return y1;
	}
	
	public int getZ1() {
		return z1;
	}
	
	public int getX2() {
		return x2;
	}
	
	public int getY2() {
		return y2;
	}
	
	public int getZ2() {
		return z2;
	}
	
}