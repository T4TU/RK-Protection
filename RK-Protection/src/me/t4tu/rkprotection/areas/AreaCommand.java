package me.t4tu.rkprotection.areas;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkprotection.Protection;

public class AreaCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		String tc4 = CoreUtils.getErrorHighlightColor();
		
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
								p.sendMessage(tc2 + " - " + tc1 + "#" + area.getId() + ": '" + area.getName() + "'");
							}
						}
						p.sendMessage("");
					}
					else if (args[0].equalsIgnoreCase("add")) {
						if (args.length >= 3) {
							try {
								int id = Integer.parseInt(args[1]);
								String name = args[2];
								Protection.getPlugin().getConfig().set("areas." + id + ".name", name);
								Protection.getPlugin().saveConfig();
								Protection.getAreaManager().loadAreasFromConfig();
								p.sendMessage(tc2 + "Asetettiin alue " + tc1 + name + tc2 + " (ID: #" + id + ")");
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheelliset argumentit!");
							}
						}
						else {
							p.sendMessage(usage + "/area add <ID> <nimi>");
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
							Area area = Protection.getAreaManager().getAreaById(args[1]);
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
							Area area = Protection.getAreaManager().getAreaById(args[1]);
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
					else if (args[0].equalsIgnoreCase("listborder")) {
						if (args.length >= 2) {
							Area area = Protection.getAreaManager().getAreaById(args[1]);
							if (area != null) {
								p.sendMessage("");
								p.sendMessage(tc2 + "§m----------" + tc1 + " Alueen rajat " + tc2 + "§m----------");
								p.sendMessage("");
								p.sendMessage(tc1 + " ID: " + tc2 + area.getId());
								p.sendMessage(tc1 + " Nimi: " + tc2 + area.getName());
								p.sendMessage(tc1 + " Rajat: " + tc2 + "(" + area.getSubAreas().size() + ")");
								if (area.getSubAreas().isEmpty()) {
									p.sendMessage(tc3 + "  Ei rajoja!");
								}
								else {
									for (SubArea subArea : area.getSubAreas()) {
										p.sendMessage(tc2 + "  - " + tc1 + subArea.getName());
									}
								}
								p.sendMessage("");
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area listborder <ID>");
						}
					}
					else if (args[0].equalsIgnoreCase("addborder")) {
						if (args.length >= 6) {
							Area area = Protection.getAreaManager().getAreaById(args[1]);
							if (area != null) {
								try {
									String name = args[2];
									int x = Integer.parseInt(args[3]);
									int y = Integer.parseInt(args[4]);
									int z = Integer.parseInt(args[5]);
									Location location1 = p.getLocation();
									Location location2 = new Location(p.getWorld(), x, y, z);
									SubArea subArea = new SubArea(name, location1, location2);
									area.addSubArea(subArea);
									p.sendMessage(tc2 + "Lisättiin uusi raja " + tc1 + subArea.getName() + tc2 + " alueelle " + tc1 + area.getName() + tc2 + "!");
								}
								catch (NumberFormatException e) {
									p.sendMessage(tc3 + "Virheelliset koordinaatit!");
								}
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else if (args.length == 4) {
							Area area = Protection.getAreaManager().getAreaById(args[1]);
							if (area != null) {
								try {
									String name = args[2];
									int radius = Integer.parseInt(args[3]);
									Location location1 = p.getLocation().clone().subtract(radius, 0, radius);
									location1.setY(0);
									Location location2 = p.getLocation().clone().add(radius, 0, radius);
									location2.setY(256);
									SubArea subArea = new SubArea(name, location1, location2);
									area.addSubArea(subArea);
									p.sendMessage(tc2 + "Lisättiin uusi raja " + tc1 + subArea.getName() + tc2 + " alueelle " + tc1 + area.getName() + tc2 + "!");
								}
								catch (NumberFormatException e) {
									p.sendMessage(tc3 + "Virheellinen säde!");
								}
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area addborder <ID> <nimi> <x2> <y2> <z2> (alkaa omasta sijainnista)" + tc3 + " tai " + tc4 + "/area addborder <ID> <nimi> <säde>");
						}
					}
					else if (args[0].equalsIgnoreCase("removeborder")) {
						if (args.length >= 3) {
							Area area = Protection.getAreaManager().getAreaById(args[1]);
							if (area != null) {
								SubArea subArea = area.getSubAreaByName(args[2]);
								if (subArea != null) {
									area.removeSubArea(subArea.getName());
									p.sendMessage(tc2 + "Poistettiin raja " + tc1 + subArea.getName() + tc2 + " alueelta " + tc1 + area.getName() + tc2 + "!");
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty rajaa antamallasi nimellä!");
								}
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area removeborder <ID> <nimi>");
						}
					}
					else if (args[0].equalsIgnoreCase("setrespawn")) {
						if (args.length >= 2) {
							Area area = Protection.getAreaManager().getAreaById(args[1]);
							if (area != null) {
								CoreUtils.setLocation(Protection.getPlugin(), "areas." + area.getId() + ".respawn-location", p.getLocation());
								area.setRespawnLocation(p.getLocation());
								p.sendMessage(tc2 + "Asetettiin alueen " + tc1 + area.getName() + tc2 + " respawn-sijainniksi nykyinen sijaintisi!");
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area setrespawn <ID>");
						}
					}
					else if (args[0].equalsIgnoreCase("setdenymessage")) {
						if (args.length >= 3) {
							Area area = Protection.getAreaManager().getAreaById(args[1]);
							if (area != null) {
								String message = "";
								for (int i = 2; i < args.length; i++) {
									message += " " + args[i];
								}
								message = message.trim();
								Protection.getPlugin().getConfig().set("areas." + area.getId() + ".deny-message", message);
								Protection.getPlugin().saveConfig();
								area.setDenyMessage(message);
								p.sendMessage(tc2 + "Asetettiin alueen " + tc1 + area.getName() + tc2 + " \"deny-message\"!");
							}
							else {
								p.sendMessage(tc3 + "Ei löydetty aluetta antamallasi ID:llä!");
							}
						}
						else {
							p.sendMessage(usage + "/area setdenymessage <ID> <viesti>");
						}
					}
					else if (args[0].equalsIgnoreCase("reload")) {
						Protection.getPlugin().reloadConfig();
						Protection.getAreaManager().loadAreasFromConfig();
						p.sendMessage(tc2 + "Ladattiin alueet uudestaan!");
					}
					else {
						p.sendMessage(usage + "/area list/add/remove/flags/flag/listborder/addborder/removeborder/setrespawn/setdenymessage/reload");
					}
				}
				else {
					p.sendMessage(usage + "/area list/add/remove/flags/flag/listborder/addborder/removeborder/setdenymessage/setrespawn/reload");
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
		else if (cmd.getName().equalsIgnoreCase("clearfire")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				Protection.getAreaListener().getFireSpreadCooldown().clear();
				sender.sendMessage(tc2 + "Komento suoritettiin onnistuneesti!");
			}
		}
		return true;
	}	
}