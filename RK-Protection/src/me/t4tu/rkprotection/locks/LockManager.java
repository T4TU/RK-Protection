package me.t4tu.rkprotection.locks;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;

import me.t4tu.rkprotection.Protection;

public class LockManager {
	
	private ArrayList<String> bypass = new ArrayList<String>();
	
	///////////////////////////////////////////////////////////////
	//
	//          Public
	//
	///////////////////////////////////////////////////////////////
	
	public ArrayList<String> getBypasses() {
		return bypass;
	}
	
	public void lock(Block block, Player player) {
		if (getConfig().getConfigurationSection("locks") != null) {
			int id = getConfig().getConfigurationSection("locks").getKeys(false).size();
			getConfig().set("locks." + id + ".uuid", player.getUniqueId().toString());
			getConfig().set("locks." + id + ".name", player.getName());
			getConfig().set("locks." + id + ".world", block.getWorld().getName());
			getConfig().set("locks." + id + ".x", block.getLocation().getBlockX());
			getConfig().set("locks." + id + ".y", block.getLocation().getBlockY());
			getConfig().set("locks." + id + ".z", block.getLocation().getBlockZ());
			getConfig().set("locks." + id + ".permissions", "^");
			saveConfig();
		}
		else {
			int id = 0;
			getConfig().set("locks." + id + ".uuid", player.getUniqueId().toString());
			getConfig().set("locks." + id + ".name", player.getName());
			getConfig().set("locks." + id + ".world", block.getWorld().getName());
			getConfig().set("locks." + id + ".x", block.getLocation().getBlockX());
			getConfig().set("locks." + id + ".y", block.getLocation().getBlockY());
			getConfig().set("locks." + id + ".z", block.getLocation().getBlockZ());
			getConfig().set("locks." + id + ".permissions", "^");
			saveConfig();
		}
	}
	
	public void unLock(Block block) {
		Block realBlock = getRealLockedBlock(block);
		if (realBlock != null) {
			Location location = realBlock.getLocation();
			for (String s : getConfig().getConfigurationSection("locks").getKeys(false)) {
				String world = location.getWorld().getName();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				if (getConfig().getString("locks." + s + ".world").equals(world) && getConfig().getInt("locks." + s + ".x") == x && 
						getConfig().getInt("locks." + s + ".y") == y && getConfig().getInt("locks." + s + ".z") == z) {
					getConfig().set("locks." + s, null);
					saveConfig();
				}
			}
		}
	}
	
	public boolean isLocked(Block block) {
		if (getRealLockedBlock(block) != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String getOwnerUuid(Block block) {
		Block realBlock = getRealLockedBlock(block);
		if (realBlock != null) {
			return getOwnerUuidExact(realBlock.getLocation());
		}
		else {
			return null;
		}
	}
	
	public String getOwnerName(Block block) {
		Block realBlock = getRealLockedBlock(block);
		if (realBlock != null) {
			return getOwnerNameExact(realBlock.getLocation());
		}
		else {
			return null;
		}
	}
	
	public ArrayList<String> getPermissions(Block block) {
		Block realBlock = getRealLockedBlock(block);
		if (realBlock != null) {
			return getPermissionsExact(realBlock.getLocation());
		}
		else {
			return null;
		}
	}
	
	public void setPermissions(Block block, ArrayList<String> permissions) {
		Block realBlock = getRealLockedBlock(block);
		if (realBlock != null) {
			setPermissionsExact(realBlock.getLocation(), permissions);
		}
	}
	
	public void setPermissions(Block block, String permission) {
		Block realBlock = getRealLockedBlock(block);
		if (realBlock != null) {
			ArrayList<String> permissions = new ArrayList<String>();
			permissions.add(permission);
			setPermissionsExact(realBlock.getLocation(), permissions);
		}
	}
	
	public boolean isOwner(Block block, Player player) {
		
		if (bypass.contains(player.getName())) {
			return true;
		}
		
		String uuid = getOwnerUuid(block);
		if (uuid != null) {
			return uuid.equals(player.getUniqueId().toString());
		}
		else {
			return false;
		}
	}
	
	public boolean hasPermission(Block block, Player player) {
		
		if (bypass.contains(player.getName())) {
			return true;
		}
		
		String uuid = getOwnerUuid(block);
		if (uuid != null) {
			if (uuid.equals(player.getUniqueId().toString())) {
				return true;
			}
			else {
				ArrayList<String> permissions = getPermissions(block);
				if (permissions.contains("*")) {
					return true;
				}
				else {
					for (String permission : permissions) {
						if (permission.equals(player.getUniqueId().toString())) {
							return true;
						}
					}
					return false;
				}
			}
		}
		else {
			return false;
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Private
	//
	///////////////////////////////////////////////////////////////
	
	private FileConfiguration getConfig() {
		return Protection.getPlugin().getConfig();
	}
	
	private void saveConfig() {
		Protection.getPlugin().saveConfig();
	}
	
	private boolean isLockedExact(Location location) {
		if (getConfig().getConfigurationSection("locks") != null) {
			for (String s : getConfig().getConfigurationSection("locks").getKeys(false)) {
				String world = location.getWorld().getName();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				if (getConfig().getString("locks." + s + ".world").equals(world) && getConfig().getInt("locks." + s + ".x") == x && 
						getConfig().getInt("locks." + s + ".y") == y && getConfig().getInt("locks." + s + ".z") == z) {
					return true;
				}
			}
		}
		return false;
	}
	
	private String getOwnerUuidExact(Location location) {
		if (getConfig().getConfigurationSection("locks") != null) {
			for (String s : getConfig().getConfigurationSection("locks").getKeys(false)) {
				String world = location.getWorld().getName();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				if (getConfig().getString("locks." + s + ".world").equals(world) && getConfig().getInt("locks." + s + ".x") == x && 
						getConfig().getInt("locks." + s + ".y") == y && getConfig().getInt("locks." + s + ".z") == z) {
					return getConfig().getString("locks." + s + ".uuid");
				}
			}
		}
		return null;
	}
	
	private String getOwnerNameExact(Location location) {
		if (getConfig().getConfigurationSection("locks") != null) {
			for (String s : getConfig().getConfigurationSection("locks").getKeys(false)) {
				String world = location.getWorld().getName();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				if (getConfig().getString("locks." + s + ".world").equals(world) && getConfig().getInt("locks." + s + ".x") == x && 
						getConfig().getInt("locks." + s + ".y") == y && getConfig().getInt("locks." + s + ".z") == z) {
					return getConfig().getString("locks." + s + ".name");
				}
			}
		}
		return null;
	}
	
	private ArrayList<String> getPermissionsExact(Location location) {
		if (getConfig().getConfigurationSection("locks") != null) {
			for (String s : getConfig().getConfigurationSection("locks").getKeys(false)) {
				String world = location.getWorld().getName();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				if (getConfig().getString("locks." + s + ".world").equals(world) && getConfig().getInt("locks." + s + ".x") == x && 
						getConfig().getInt("locks." + s + ".y") == y && getConfig().getInt("locks." + s + ".z") == z) {
					ArrayList<String> permissions = new ArrayList<String>();
					for (String permission : getConfig().getString("locks." + s + ".permissions").split(";")) {
						permissions.add(permission);
					}
					return permissions;
				}
			}
		}
		return null;
	}
	
	private void setPermissionsExact(Location location, ArrayList<String> permissions) {
		if (getConfig().getConfigurationSection("locks") != null) {
			for (String s : getConfig().getConfigurationSection("locks").getKeys(false)) {
				String world = location.getWorld().getName();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				if (getConfig().getString("locks." + s + ".world").equals(world) && getConfig().getInt("locks." + s + ".x") == x && 
						getConfig().getInt("locks." + s + ".y") == y && getConfig().getInt("locks." + s + ".z") == z) {
					String newPermissions = "";
					for (String permission : permissions) {
						newPermissions = newPermissions + ";" + permission;
					}
					if (newPermissions.startsWith(";")) {
						newPermissions = newPermissions.substring(1, newPermissions.length());
					}
					if (newPermissions.endsWith(";")) {
						newPermissions = newPermissions.substring(0, newPermissions.length() - 1);
					}
					getConfig().set("locks." + s + ".permissions", newPermissions);
					saveConfig();
				}
			}
		}
	}
	
	private Block getRealLockedBlock(Block block) {
		Location location = block.getLocation();
		if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.BIRCH_DOOR || block.getType() == Material.SPRUCE_DOOR || block.getType() == Material.JUNGLE_DOOR || block.getType() == Material.ACACIA_DOOR || block.getType() == Material.DARK_OAK_DOOR) {
			Door door = (Door)block.getState().getData();
			if (door.isTopHalf()) {
				if (isLockedExact(block.getRelative(BlockFace.DOWN).getLocation())) {
					return block.getRelative(BlockFace.DOWN);
				}
				else {
					return null;
				}
			}
			else {
				if (isLockedExact(block.getLocation())) {
					return block;
				}
				else {
					return null;
				}
			}
		}
		else if (block.getType() == Material.CHEST) {
			if (isLockedExact(location)) {
				return block;
			}
			else if (block.getRelative(BlockFace.NORTH).getType() == Material.CHEST && isLockedExact(block.getRelative(BlockFace.NORTH).getLocation())) {
				return block.getRelative(BlockFace.NORTH);
			}
			else if (block.getRelative(BlockFace.SOUTH).getType() == Material.CHEST && isLockedExact(block.getRelative(BlockFace.SOUTH).getLocation())) {
				return block.getRelative(BlockFace.SOUTH);
			}
			else if (block.getRelative(BlockFace.EAST).getType() == Material.CHEST && isLockedExact(block.getRelative(BlockFace.EAST).getLocation())) {
				return block.getRelative(BlockFace.EAST);
			}
			else if (block.getRelative(BlockFace.WEST).getType() == Material.CHEST && isLockedExact(block.getRelative(BlockFace.WEST).getLocation())) {
				return block.getRelative(BlockFace.WEST);
			}
			else {
				return null;
			}
		}
		else if (block.getType() == Material.TRAPPED_CHEST) {
			if (isLockedExact(location)) {
				return block;
			}
			else if (block.getRelative(BlockFace.NORTH).getType() == Material.TRAPPED_CHEST && isLockedExact(block.getRelative(BlockFace.NORTH).getLocation())) {
				return block.getRelative(BlockFace.NORTH);
			}
			else if (block.getRelative(BlockFace.SOUTH).getType() == Material.TRAPPED_CHEST && isLockedExact(block.getRelative(BlockFace.SOUTH).getLocation())) {
				return block.getRelative(BlockFace.SOUTH);
			}
			else if (block.getRelative(BlockFace.EAST).getType() == Material.TRAPPED_CHEST && isLockedExact(block.getRelative(BlockFace.EAST).getLocation())) {
				return block.getRelative(BlockFace.EAST);
			}
			else if (block.getRelative(BlockFace.WEST).getType() == Material.TRAPPED_CHEST && isLockedExact(block.getRelative(BlockFace.WEST).getLocation())) {
				return block.getRelative(BlockFace.WEST);
			}
			else {
				return null;
			}
		}
		else {
			if (isLockedExact(location)) {
				return block;
			}
			else {
				return null;
			}
		}
	}
}