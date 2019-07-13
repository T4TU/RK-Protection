package me.t4tu.rkprotection.locks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkprotection.Protection;
import me.t4tu.rkprotection.areas.Flag;

public class LockCommand implements CommandExecutor {
	
	private Map<String, String> lockActions = new HashMap<String, String>();
	private Map<String, String> multiLockActions = new HashMap<String, String>();
	
	public Map<String, String> getLockActions() {
		return lockActions;
	}
	
	public Map<String, String> getMultiLockActions() {
		return multiLockActions;
	}
	
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
		
		if (cmd.getName().equalsIgnoreCase("bypass")) {
			if (CoreUtils.hasRank(p, "valvoja")) {
				if (Protection.getLockManager().getBypasses().contains(p.getName())) {
					Protection.getLockManager().getBypasses().remove(p.getName());
					p.sendMessage(tc2 + "Et voi enää käyttää lukittuja kohteita!");
				}
				else {
					Protection.getLockManager().getBypasses().add(p.getName());
					p.sendMessage(tc2 + "Voit nyt käyttää lukittuja kohteita!");
				}
			}
			else {
				p.sendMessage(noPermission);
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("lukko")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("lukitse") || args[0].equalsIgnoreCase("avaa") || args[0].equalsIgnoreCase("oikeudet")) {
					String c = "";
					for (String s : args) {
						c = c + " " + s;
					}
					p.performCommand(c.trim());
				}
				else {
					p.sendMessage(tc3 + "Ei löydetty alikomentoa " + tc4 + args[0] + tc3 + "!");
					p.sendMessage(tc3 + "Saatavilla olevat alikomennot ovat: " + tc4 + "lukitse" + tc3 + ", " + tc4 + "avaa" + tc3 + " ja " + tc4 + "oikeudet");
				}
			}
			else {
				p.sendMessage("");
				p.sendMessage("§b§lOvien, arkkujen yms. lukitseminen:");
				p.sendMessage("");
				p.sendMessage(" §e/lukko lukitse§b tai §e/lukitse§b - lukitse kohde");
				p.sendMessage(" §e/lukko avaa§b tai §e/avaa§b - poista lukitus kohteesta");
				p.sendMessage(" §e/lukko oikeudet <nimi>§b tai §e/oikeudet <nimi>§b - anna toiselle pelaajalle (tai poista) oikeudet käyttää lukittuja kohteitasi");
				p.sendMessage("");
				p.sendMessage(" §bAntaessasi oikeuksia, voit käyttää pelaajan nimen sijasta myös seuraavia merkintöjä: §e*punakivi*§b, §e*suppilot*§b ja §e*kaikki*");
				p.sendMessage("");
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("lukitse")) {
			if (multiLockActions.containsKey(p.getName())) {
				multiLockActions.remove(p.getName());
				p.sendMessage(tc2 + "Poistuttiin lukitustilasta!");
				p.sendTitle("", "", 0, 10, 0);
				return true;
			}
			lockActions.remove(p.getName());
			multiLockActions.remove(p.getName());
			if (args.length >= 1 && args[0].equalsIgnoreCase("*")) {
				multiLockActions.put(p.getName(), "lukitse");
				p.sendMessage(tc2 + "Klikkaa kohteita, jotka haluat lukita. Poistu kirjoittamalla " + tc1 + "/lukitse" + tc2 + ".");
				p.sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
			}
			else {
				lockActions.put(p.getName(), "lukitse");
				p.sendMessage(tc2 + "Klikkaa kohdetta, jonka haluat lukita...");
				p.sendTitle("", "§fKlikkaa haluamaasi kohdetta...", 0, 200, 20);
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("avaa")) {
			if (multiLockActions.containsKey(p.getName())) {
				multiLockActions.remove(p.getName());
				p.sendMessage(tc2 + "Poistuttiin avaustilasta!");
				p.sendTitle("", "", 0, 10, 0);
				return true;
			}
			lockActions.remove(p.getName());
			multiLockActions.remove(p.getName());
			if (args.length >= 1 && args[0].equalsIgnoreCase("*")) {
				multiLockActions.put(p.getName(), "avaa");
				p.sendMessage(tc2 + "Klikkaa kohteita, jotka haluat avata. Poistu kirjoittamalla " + tc1 + "/avaa" + tc2 + ".");
				p.sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
			}
			else {
				lockActions.put(p.getName(), "avaa");
				p.sendMessage(tc2 + "Klikkaa kohdetta, jonka haluat avata...");
				p.sendTitle("", "§fKlikkaa haluamaasi kohdetta...", 0, 200, 20);
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("oikeudet")) {
			if (args.length >= 1) {
				String s = "";
				for (String arg : args) {
					s += " " + arg;
				}
				if (multiLockActions.containsKey(p.getName())) {
					multiLockActions.remove(p.getName());
					p.sendMessage(tc2 + "Poistuttiin oikeustilasta!");
					p.sendTitle("", "", 0, 10, 0);
					return true;
				}
				lockActions.remove(p.getName());
				multiLockActions.remove(p.getName());
				if (args.length >= 2 && args[1].equalsIgnoreCase("*")) {
					multiLockActions.put(p.getName(), "oikeudet" + s);
					p.sendMessage(tc2 + "Klikkaa kohteita, joiden oikeuksia haluat muuttaa. Poistu kirjoittamalla " + tc1 + "/oikeudet" + tc2 + ".");
					p.sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
				}
				else {
					lockActions.put(p.getName(), "oikeudet" + s);
					p.sendMessage(tc2 + "Klikkaa kohdetta, jonka oikeuksia haluat muuttaa...");
					p.sendTitle("", "§fKlikkaa haluamaasi kohdetta...", 0, 200, 20);
				}
			}
			else {
				if (multiLockActions.containsKey(p.getName())) {
					multiLockActions.remove(p.getName());
					p.sendMessage(tc2 + "Poistuttiin oikeustilasta!");
					p.sendTitle("", "", 0, 10, 0);
				}
				else {
					p.sendMessage(usage + "/oikeudet <nimi>");
				}
			}
		}
		return true;
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Lukitse
	//
	///////////////////////////////////////////////////////////////
	
	public void lukitse(Player p, Block b, String kuutio, String kuution, String kuutioita, String tc2, String tc3) {
		if (Protection.getLockManager().isLocked(b)) {
			p.sendMessage(tc3 + "Tämä " + kuutio + " on jo lukittu!");
			p.sendTitle("§4§l✖", "", 5, 15, 5);
			p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
		}
		else if (Protection.getAreaManager().getArea(b.getLocation()) != null && !Protection.getAreaManager().getArea(b.getLocation()).hasFlag(Flag.ALLOW_LOCKING) && !CoreUtils.hasRank(p, "ylläpitäjä")) {
			p.sendMessage(tc3 + "Et voi lukita " + kuutioita + " tällä alueella!");
			p.sendTitle("§4§l✖", "", 5, 15, 5);
			p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
		}
		else {
			Protection.getLockManager().lock(b, p);
			p.sendMessage(tc2 + "Lukitsit " + kuution + ".");
			p.sendTitle("§2§l✓", "", 5, 15, 5);
			p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Avaa
	//
	///////////////////////////////////////////////////////////////
	
	public void avaa(Player p, Block b, String kuutio, String kuution, String tc2, String tc3) {
		if (Protection.getLockManager().isLocked(b)) {
			if (Protection.getLockManager().isOwner(b, p)) {
				Protection.getLockManager().unLock(b);
				p.sendMessage(tc2 + "Poistettiin " + kuution + " lukitus.");
				p.sendTitle("§2§l✓", "", 5, 15, 5);
				p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
			}
			else {
				p.sendMessage(tc3 + "Et voi poistaa toisen pelaajan " + kuution + " lukitusta!");
				p.sendTitle("§4§l✖", "", 5, 15, 5);
				p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
			}
		}
		else {
			p.sendMessage(tc3 + "Tämä " + kuutio + " ei ole lukossa!");
			p.sendTitle("§4§l✖", "", 5, 15, 5);
			p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Oikeudet
	//
	///////////////////////////////////////////////////////////////
	
	public void oikeudet(Player p, Block b, String[] args, String kuutio, String kuutiota, String kuution, String kuutioihin, String tc1, String tc2, String tc3) {
		if (Protection.getLockManager().isLocked(b)) {
			if (Protection.getLockManager().isOwner(b, p)) {
				ArrayList<String> permissions = Protection.getLockManager().getPermissions(b);
				if (args[0].equalsIgnoreCase("*kaikki*")) {
					if (permissions.contains("*")) {
						Protection.getLockManager().setPermissions(b, "^");
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						p.sendMessage(tc2 + "Poistettiin " + tc1 + "kaikilta" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
						p.sendTitle("§2§l✓", "", 5, 15, 5);
					}
					else {
						Protection.getLockManager().setPermissions(b, "*");
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						p.sendMessage(tc2 + "Lisättiin " + tc1 + "kaikille" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
						p.sendTitle("§2§l✓", "", 5, 15, 5);
					}
				}
				else if (args[0].equals(p.getName())) {
					p.sendMessage(tc3 + "Et voi poistaa oikeuksia itseltäsi!");
					p.sendTitle("§4§l✖", "", 5, 15, 5);
					p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
				}
				else {
					if (args[0].equalsIgnoreCase("*punakivi*")) {
						if (permissions.contains("+")) {
							permissions.remove("+");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Poistettiin " + tc1 + "punakiveltä" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
							p.sendTitle("§2§l✓", "", 5, 15, 5);
						}
						else {
							if (permissions.contains("*")) {
								p.sendMessage(tc3 + "Kaikilla pelaajilla, suppiloilla ja punakivellä on jo oikeudet käyttää tätä " + kuutiota + "!");
								p.sendTitle("§4§l✖", "", 5, 15, 5);
								p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
								return;
							}
							permissions.add("+");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Lisättiin " + tc1 + "punakivelle" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
							p.sendTitle("§2§l✓", "", 5, 15, 5);
						}
					}
					else if (args[0].equalsIgnoreCase("*suppilot*")) {
						if (permissions.contains("_")) {
							permissions.remove("_");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Poistettiin " + tc1 + "suppiloilta" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
							p.sendTitle("§2§l✓", "", 5, 15, 5);
						}
						else {
							if (permissions.contains("*")) {
								p.sendMessage(tc3 + "Kaikilla pelaajilla, suppiloilla ja punakivellä on jo oikeudet käyttää tätä " + kuutiota + "!");
								p.sendTitle("§4§l✖", "", 5, 15, 5);
								p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
								return;
							}
							permissions.add("_");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Lisättiin " + tc1 + "suppiloille" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
							p.sendTitle("§2§l✓", "", 5, 15, 5);
						}
					}
					else {
						new BukkitRunnable() {
							public void run() {
								MySQLResult infoData = MySQLUtils.get("SELECT uuid, name FROM player_info WHERE name=?", args[0]);
								if (infoData != null) {
									String uuid = infoData.getString(0, "uuid");
									String name = infoData.getString(0, "name");
									if (permissions.contains(uuid)) {
										permissions.remove(uuid);
										Protection.getLockManager().setPermissions(b, permissions);
										p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
										p.sendMessage(tc2 + "Poistettiin oikeudet käyttää tätä " + kuutiota + " pelaajalta " + tc1 + name + tc2 + "!");
										p.sendTitle("§2§l✓", "", 5, 15, 5);
									}
									else {
										if (permissions.contains("*")) {
											p.sendMessage(tc3 + "Kaikilla pelaajilla on jo oikeudet käyttää tätä " + kuutiota + "!");
											p.sendTitle("§4§l✖", "", 5, 15, 5);
											p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
											return;
										}
										permissions.add(uuid);
										Protection.getLockManager().setPermissions(b, permissions);
										p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
										p.sendMessage(tc2 + "Lisättiin oikeudet käyttää tätä " + kuutiota + " pelaajalle " + tc1 + name + tc2 + "!");
										p.sendTitle("§2§l✓", "", 5, 15, 5);
									}
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
									p.sendTitle("§4§l✖", "", 5, 15, 5);
									p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
								}
							}
						}.runTaskAsynchronously(Protection.getPlugin());
					}
				}
			}
			else {
				p.sendMessage(tc3 + "Et voi lisätä oikeuksia muiden " + kuutioihin + "!");
				p.sendTitle("§4§l✖", "", 5, 15, 5);
				p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
			}
		}
		else {
			p.sendMessage(tc3 + "Tämä " + kuutio + " ei ole lukossa!");
			p.sendTitle("§4§l✖", "", 5, 15, 5);
			p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
		}
	}
}