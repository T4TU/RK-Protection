package me.t4tu.rkprotection.locks;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkprotection.Protection;
import me.t4tu.rkprotection.areas.Flag;

public class LockCommand implements CommandExecutor {
	
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
			if (CoreUtils.hasRank(p, "moderaattori")) {
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
				p.sendMessage(" §e/lukko lukitse§b tai §e/lukitse§b - lukitse katsomasi kohde");
				p.sendMessage(" §e/lukko avaa§b tai §e/avaa§b - poista lukitus katsomastasi kohteesta");
				p.sendMessage(" §e/lukko oikeudet <nimi>§b tai §e/oikeudet <nimi>§b - anna toiselle pelaajalle (tai poista) oikeudet käyttää lukittuja kohteitasi");
				p.sendMessage("");
				p.sendMessage(" §bAntaessasi oikeuksia, voit käyttää pelaajan nimen sijasta myös seuraavia merkintöjä: §e*punakivi*§b, §e*suppilot*§b ja §e*kaikki*");
				p.sendMessage("");
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("lukitse")) {
			Block b = p.getTargetBlock((Set<Material>) null, 10);
			if (b.getType() == Material.AIR) {
				p.sendMessage(tc3 + "Sinun täytyy seisoa " + tc4 + "oven" + tc3 + ", " + tc4 + "arkun" + tc3 + ", " + tc4 + "uunin" + tc3 + ", " + tc4 + "portin" + tc3 + " tai " + tc4 + "ansaluukun\n" + tc3 + " vieressä ja katsoa sitä kohti voidaksesi lukita sen.");
			}
			else if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR) {
				Door d = (Door) b.getState().getData();
				if (p.getLocation().distance(b.getLocation()) > 5) {
					p.sendMessage(tc3 + "Olet liian kaukana voidaksesi lukita tämän oven!");
				}
				else if (Protection.getLockManager().isLocked(b)) {
					p.sendMessage(tc3 + "Tämä ovi on jo lukittu!");
					p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
				}
				else if (Protection.getAreaManager().getArea(b.getLocation()) != null && !Protection.getAreaManager().getArea(b.getLocation()).hasFlag(Flag.ALLOW_LOCKING) && !CoreUtils.hasRank(p, "ylläpitäjä")) {
					p.sendMessage(tc3 + "Et voi lukita ovia tällä alueella!");
					p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
				}
				else {
					if (d.isTopHalf()) {
						Protection.getLockManager().lock(b.getRelative(BlockFace.DOWN), p);
					}
					else {
						Protection.getLockManager().lock(b, p);
					}
					p.sendMessage(tc2 + "Lukitsit oven. Kukaan muu ei voi enää avata tai sulkea sitä.");
					p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
				}
			}
			else if (b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
				lukitse(p, b, "portti", "portin", "portteja", tc2, tc3);
			}
			else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				lukitse(p, b, "arkku", "arkun", "arkkuja", tc2, tc3);
			}
			else if (b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE) {
				lukitse(p, b, "uuni", "uunin", "uuneja", tc2, tc3);
			}
			else if (b.getType() == Material.TRAP_DOOR) {
				lukitse(p, b, "ansaluukku", "ansaluukun", "ansaluukkuja", tc2, tc3);
			}
			else {
				p.sendMessage(tc3 + "Voit lukita ainoastaan " + tc4 + "ovia" + tc3 + ", " + tc4 + "arkkuja" + tc3 + ", " + tc4 + "uuneja" + tc3 + ", " + tc4 + "portteja" + tc3 + " ja " + tc4 + "ansaluukkuja" + tc3 + ".");
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("avaa")) {
			Block b = p.getTargetBlock((Set<Material>) null, 10);
			if (b.getType() == Material.AIR) {
				p.sendMessage(tc3 + "Sinun täytyy seisoa " + tc4 + "oven" + tc3 + ", " + tc4 + "arkun" + tc3 + ", " + tc4 + "uunin" + tc3 + ", " + tc4 + "portin" + tc3 + " tai " + tc4 + "ansaluukun\n" + tc3 + " vieressä ja katsoa sitä kohti voidaksesi poistaa sen lukituksen.");
			}
			else if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR) {
				avaa(p, b, "ovi", "oven", "ovesta", "ovista", tc2, tc3);
			}
			else if (b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
				avaa(p, b, "portti", "portin", "portista", "porteista", tc2, tc3);
			}
			else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				avaa(p, b, "arkku", "arkun", "arkusta", "arkuista", tc2, tc3);
			}
			else if (b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE) {
				avaa(p, b, "uuni", "uunin", "uunista", "uuneista", tc2, tc3);
			}
			else if (b.getType() == Material.TRAP_DOOR) {
				avaa(p, b, "ansaluukku", "ansaluukun", "ansaluukusta", "ansaluukuista", tc2, tc3);
			}
			else {
				p.sendMessage(tc3 + "Voit poistaa lukituksen ainoastaan lukituista kohteista.");
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("oikeudet")) {
			if (args.length >= 1) {
				Block b = p.getTargetBlock((Set<Material>) null, 10);
				if (b.getType() == Material.AIR) {
					p.sendMessage(tc3 + "Sinun täytyy seisoa " + tc4 + "oven" + tc3 + ", " + tc4 + "arkun" + tc3 + ", " + tc4 + "uunin" + tc3 + ", " + tc4 + "portin" + tc3 + " tai " + tc4 + "ansaluukun\n" + tc3 + " vieressä ja katsoa sitä kohti voidaksesi muokata sen oikeuksia.");
				}
				else if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR) {
					oikeudet(p, b, args, "ovi", "ovea", "oveen", "oven", tc1, tc2, tc3);
				}
				else if (b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
					oikeudet(p, b, args, "portti", "porttia", "porttin", "portteihin", tc1, tc2, tc3);
				}
				else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
					oikeudet(p, b, args, "arkku", "arkkua", "arkkun", "arkkuihin", tc1, tc2, tc3);
				}
				else if (b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE) {
					oikeudet(p, b, args, "uuni", "uunia", "uunin", "uuneihin", tc1, tc2, tc3);
				}
				else if (b.getType() == Material.TRAP_DOOR) {
					oikeudet(p, b, args, "ansaluukku", "ansaluukkua", "ansaluukun", "ansaluukkuihin", tc1, tc2, tc3);
				}
				else {
					p.sendMessage(tc3 + "Voit muokata ainoastaan lukittujen kohteiden oikeuksia!");
				}
			}
			else {
				p.sendMessage(usage + "/oikeudet <pelaaja>");
			}
		}
		return true;
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Lukitse
	//
	///////////////////////////////////////////////////////////////
	
	private void lukitse(Player p, Block b, String kuutio, String kuution, String kuutioita, String tc2, String tc3) {
		if (p.getLocation().distance(b.getLocation()) > 5) {
			p.sendMessage(tc3 + "Olet liian kaukana voidaksesi lukita tämän " + kuution + "!");
		}
		else if (Protection.getLockManager().isLocked(b)) {
			p.sendMessage(tc3 + "Tämä " + kuutio + " on jo lukittu!");
			p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
		}
		else if (Protection.getAreaManager().getArea(b.getLocation()) != null && !Protection.getAreaManager().getArea(b.getLocation()).hasFlag(Flag.ALLOW_LOCKING) && !CoreUtils.hasRank(p, "ylläpitäjä") && !p.isOp()) {
			p.sendMessage(tc3 + "Et voi lukita " + kuutioita + " tällä alueella!");
			p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
		}
		else {
			Protection.getLockManager().lock(b, p);
			p.sendMessage(tc2 + "Lukitsit " + kuution + ". Kukaan muu ei voi enää avata tai sulkea sitä.");
			p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Avaa
	//
	///////////////////////////////////////////////////////////////
	
	private void avaa(Player p, Block b, String kuutio, String kuution, String kuutiosta, String kuutioista, String tc2, String tc3) {
		if (p.getLocation().distance(b.getLocation()) > 5) {
			p.sendMessage(tc3 + "Olet liian kaukana poistaaksesi tämän " + kuution + " lukituksen!");
		}
		else if (Protection.getLockManager().isLocked(b)) {
			if (Protection.getLockManager().isOwner(b, p)) {
				Protection.getLockManager().unLock(b);
				p.sendMessage(tc2 + "Poistettiin lukitus " + kuutiosta + ". Kaikki voivat taas käyttää sitä.");
				p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
			}
			else {
				p.sendMessage(tc3 + "Et voi poistaa lukitusta muiden " + kuutioista + "!");
			}
		}
		else {
			p.sendMessage(tc3 + "Tämä " + kuutio + " ei ole lukossa!");
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          Oikeudet
	//
	///////////////////////////////////////////////////////////////
	
	private void oikeudet(Player p, Block b, String[] args, String kuutio, String kuutiota, String kuution, String kuutioihin, String tc1, String tc2, String tc3) {
		if (p.getLocation().distance(b.getLocation()) > 5) {
			p.sendMessage(tc3 + "Olet liian kaukana muokataksesi tämän " + kuution + " oikeuksia!");
		}
		else if (Protection.getLockManager().isLocked(b)) {
			if (Protection.getLockManager().isOwner(b, p)) {
				ArrayList<String> permissions = Protection.getLockManager().getPermissions(b);
				if (args[0].equalsIgnoreCase("*kaikki*")) {
					if (permissions.contains("*")) {
						Protection.getLockManager().setPermissions(b, "^");
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						p.sendMessage(tc2 + "Poistettiin " + tc1 + "kaikilta" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
					}
					else {
						Protection.getLockManager().setPermissions(b, "*");
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						p.sendMessage(tc2 + "Lisättiin " + tc1 + "kaikille" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
					}
				}
				else if (args[0].equals(p.getName())) {
					p.sendMessage(tc3 + "Et voi poistaa oikeuksia itseltäsi!");
				}
				else {
					if (args[0].equalsIgnoreCase("*punakivi*")) {
						if (permissions.contains("+")) {
							permissions.remove("+");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Poistettiin " + tc1 + "punakiveltä" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
						}
						else {
							if (permissions.contains("*")) {
								p.sendMessage(tc3 + "Kaikilla pelaajilla, suppiloilla ja punakivellä on jo oikeudet käyttää tätä " + kuutiota + "!");
								return;
							}
							permissions.add("+");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Lisättiin " + tc1 + "punakivelle" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
						}
					}
					else if (args[0].equalsIgnoreCase("*suppilot*")) {
						if (permissions.contains("_")) {
							permissions.remove("_");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Poistettiin " + tc1 + "suppiloilta" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
						}
						else {
							if (permissions.contains("*")) {
								p.sendMessage(tc3 + "Kaikilla pelaajilla, suppiloilla ja punakivellä on jo oikeudet käyttää tätä " + kuutiota + "!");
								return;
							}
							permissions.add("_");
							Protection.getLockManager().setPermissions(b, permissions);
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
							p.sendMessage(tc2 + "Lisättiin " + tc1 + "suppiloille" + tc2 + " oikeudet käyttää tätä " + kuutiota + "!");
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
									}
									else {
										if (permissions.contains("*")) {
											p.sendMessage(tc3 + "Kaikilla pelaajilla on jo oikeudet käyttää tätä " + kuutiota + "!");
											return;
										}
										permissions.add(uuid);
										Protection.getLockManager().setPermissions(b, permissions);
										p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
										p.sendMessage(tc2 + "Lisättiin oikeudet käyttää tätä " + kuutiota + " pelaajalle " + tc1 + name + tc2 + "!");
									}
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
								}
							}
						}.runTaskAsynchronously(Protection.getPlugin());
					}
				}
			}
			else {
				p.sendMessage(tc3 + "Et voi lisätä oikeuksia muiden " + kuutioihin + "!");
			}
		}
		else {
			p.sendMessage(tc3 + "Tämä " + kuutio + " ei ole lukossa!");
		}
	}
}