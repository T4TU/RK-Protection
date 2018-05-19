package me.t4tu.rkprotection.areas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkprotection.Protection;
import net.md_5.bungee.api.ChatMessageType;

public class AreaCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		String usage = CoreUtils.getUsageString();
		String noPermission = CoreUtils.getNoPermissionString();
		String playersOnly = CoreUtils.getPlayersOnlyString();
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(tc3 + playersOnly);
			return true;
		}
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("area")) {
			if (CoreUtils.getAdminPowers().contains(sender.getName()) || sender.isOp()) {
				if (args.length >=  1) {
					if (args[0].equalsIgnoreCase("list")) {
						p.sendMessage("");
						p.sendMessage(tc2 + "§m----------" + tc1 + " Alueet " + tc2 + "§m----------");
						p.sendMessage("");
						if (Protection.getAreaManager().getAreas().isEmpty()) {
							p.sendMessage(tc3 + " Ei alueita!");
						}
						else {
							for (Area area : Protection.getAreaManager().getAreas()) {
								ReflectionUtils.sendChatPacket(p, "[\"\",{\"text\":\"" + tc2 + " - \"},{\"text\":\"" + tc1 + "#" + area.getID() + ": '" + 
										area.getName() + "'\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/area tp " + area.getID() + 
										"\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + tc1 + "Teleporttaa alueelle '" + 
										area.getName() + "'\"}]}}}]", ChatMessageType.CHAT);
							}
						}
						p.sendMessage("");
					}
					else if (args[0].equalsIgnoreCase("tp")) {
						if (args.length >= 2) {
							if (Protection.getAreaManager().getAreaByID(args[1]) != null) {
								Area area = Protection.getAreaManager().getAreaByID(args[1]);
								Location location = new Location(Bukkit.getWorld(area.getWorld()), area.getX1() + (area.getX2() - area.getX1()) / 2, area.getY2() - 1, area.getZ1() + (area.getZ2() - area.getZ1()) / 2);
								p.sendMessage(tc2 + "Teleportataan alueelle " + area.getName() + "!");
								p.teleport(location);
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area tp <ID>");
						}
					}
					else if (args[0].equalsIgnoreCase("set")) {
						if (args.length >= 4) {
							try {
								int id = Integer.parseInt(args[1]);
								int radius = Integer.parseInt(args[2]);
								String name = args[3];
								Protection.getPlugin().getConfig().set("areas." + id + ".x1", p.getLocation().getBlockX() - radius);
								Protection.getPlugin().getConfig().set("areas." + id + ".y1", 0);
								Protection.getPlugin().getConfig().set("areas." + id + ".z1", p.getLocation().getBlockZ() - radius);
								Protection.getPlugin().getConfig().set("areas." + id + ".x2", p.getLocation().getBlockX() + radius);
								Protection.getPlugin().getConfig().set("areas." + id + ".y2", 256);
								Protection.getPlugin().getConfig().set("areas." + id + ".z2", p.getLocation().getBlockZ() + radius);
								Protection.getPlugin().getConfig().set("areas." + id + ".world", p.getWorld().getName());
								Protection.getPlugin().getConfig().set("areas." + id + ".name", name);
								Protection.getPlugin().saveConfig();
								Protection.getAreaManager().loadAreasFromConfig();
								p.sendMessage(tc2 + "Asetettiin alue '" + name + "' (ID: #" + id + ", säde: " + radius + ")");
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheelliset argumentit!");
							}
						}
						else {
							p.sendMessage(usage + "/area set <ID> <säde> <nimi>");
						}
					}
					else if (args[0].equalsIgnoreCase("remove")) {
						if (args.length >= 2) {
							try {
								int id = Integer.parseInt(args[1]);
								Protection.getPlugin().getConfig().set("areas." + id, null);
								Protection.getPlugin().saveConfig();
								Protection.getAreaManager().loadAreasFromConfig();
								p.sendMessage(tc2 + "Poistettiin alue ID:llä #" + id);
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheelliset argumentit!");
							}
						}
						else {
							p.sendMessage(usage + "/area remove <ID>");
						}
					}
					else if (args[0].equalsIgnoreCase("flags")) {
						if (args.length >= 2) {
							Area area = Protection.getAreaManager().getAreaByID(args[1]);
							if (area != null) {
								p.sendMessage("");
								p.sendMessage(tc2 + "Tämän alueen flagit:");
								String flags = "";
								for (Flag flag : area.getFlags()) {
									flags = flags + flag.toString() + " ";
								}
								p.sendMessage(tc2 + " " + flags.trim().replace(" ", ", "));
								p.sendMessage("");
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area flags <ID>");
						}
					}
					else if (args[0].equalsIgnoreCase("flag")) {
						if (args.length >= 3) {
							args[2] = args[2].toUpperCase();
							Area area = Protection.getAreaManager().getAreaByID(args[1]);
							if (area != null) {
								try {
									if (area.hasFlag(Flag.valueOf(args[2]))) {
										area.removeFlag(Flag.valueOf(args[2]));
										p.sendMessage(tc2 + "Poistettiin flagi " + Flag.valueOf(args[2]) + "!");
									}
									else {
										area.addFlag(Flag.valueOf(args[2]));
										p.sendMessage(tc2 + "Lisättiin flagi " + Flag.valueOf(args[2]) + "!");
									}
								}
								catch (Exception e) {
									p.sendMessage(tc3 + "Virheellinen flagi!");
								}
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage("");
							p.sendMessage(tc2 + "Käytettävissä olevat flagit:");
							String flags = "";
							for (Flag flag : Flag.values()) {
								flags = flags + flag.toString() + " ";
							}
							p.sendMessage(tc2 + " " + flags.trim().replace(" ", ", "));
							p.sendMessage("");
							p.sendMessage(usage + "/area flag <ID> <flagi>");
						}
					}
					else if (args[0].equalsIgnoreCase("reload")) {
						Protection.getPlugin().reloadConfig();
						Protection.getAreaManager().loadAreasFromConfig();
						p.sendMessage(tc2 + "Ladattiin alueet uudestaan!");
					}
					else {
						p.sendMessage(usage + "/area <list/tp/set/remove/flags/flag/reload>");
					}
				}
				else {
					p.sendMessage(usage + "/area <list/tp/set/remove/flags/flag/reload>");
				}
			}
			else {
				p.sendMessage(noPermission);
			}
		}
		else if (cmd.getName().equalsIgnoreCase("pvpbypass")) {
			if (CoreUtils.getAdminPowers().contains(sender.getName()) || sender.isOp()) {
				if (Protection.getAreaManager().getPvPBypasses().contains(p.getName())) {
					Protection.getAreaManager().getPvPBypasses().remove(p.getName());
					p.sendMessage(tc2 + "PvP-bypass pois käytöstä!");
				}
				else {
					Protection.getAreaManager().getPvPBypasses().add(p.getName());
					p.sendMessage(tc2 + "PvP-bypass käytössä!");
				}
			}
			else {
				p.sendMessage(noPermission);
			}
		}
		return true;
	}	
}