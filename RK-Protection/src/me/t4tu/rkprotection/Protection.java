package me.t4tu.rkprotection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkprotection.areas.AreaCommand;
import me.t4tu.rkprotection.areas.AreaListener;
import me.t4tu.rkprotection.areas.AreaManager;
import me.t4tu.rkprotection.locks.LockCommand;
import me.t4tu.rkprotection.locks.LockListener;
import me.t4tu.rkprotection.locks.LockManager;

public class Protection extends JavaPlugin {
	
	private static Plugin plugin;
	private static AreaManager areaManager = new AreaManager();
	private static AreaListener areaListener = new AreaListener();
	private static AreaCommand areaCommand = new AreaCommand();
	private static LockManager lockManager = new LockManager();
	private static LockCommand lockCommand = new LockCommand();
	private static LockListener lockListener = new LockListener();
	
	private void registerCommand(String s, CommandExecutor c, boolean tabCompletion) {
		getCommand(s).setExecutor(c);
		if (tabCompletion) {
			CoreUtils.getRegisteredCommandsWithTabCompletion().add(s);
		}
		else {
			CoreUtils.getRegisteredCommands().add(s);
		}
	}
	
	public void onEnable() {
		plugin = this;
		saveConfig();
		Bukkit.getPluginManager().registerEvents(areaListener, this);
		Bukkit.getPluginManager().registerEvents(lockListener, this);
		registerCommand("pvpbypass", areaCommand, false);
		registerCommand("area", areaCommand, false);
		registerCommand("clearfire", areaCommand, false);
		registerCommand("lukko", lockCommand, true);
		registerCommand("lukitse", lockCommand, true);
		registerCommand("avaa", lockCommand, true);
		registerCommand("oikeudet", lockCommand, true);
		registerCommand("bypass", lockCommand, false);
		areaManager.loadAreasFromConfig();
	}
	
	public void onDisable() {
		plugin = null;
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static AreaManager getAreaManager() {
		return areaManager;
	}
	
	public static AreaListener getAreaListener() {
		return areaListener;
	}
	
	public static AreaCommand getAreaCommand() {
		return areaCommand;
	}
	
	public static LockManager getLockManager() {
		return lockManager;
	}
	
	public static LockCommand getLockCommand() {
		return lockCommand;
	}
	
	public static LockListener getLockListener() {
		return lockListener;
	}	
}