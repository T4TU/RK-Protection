package me.t4tu.rkprotection.locks;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import me.t4tu.rkprotection.Protection;
import me.t4tu.rkprotection.areas.Area;
import me.t4tu.rkprotection.areas.Flag;
import net.md_5.bungee.api.ChatMessageType;

public class LockListener implements Listener {
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerCommandPreprocess
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		
		String tc3 = CoreUtils.getErrorBaseColor();
		String tc4 = CoreUtils.getErrorHighlightColor();
		
		String message = e.getMessage().substring(1).toLowerCase();
		
		if (message.equalsIgnoreCase("lwc") || message.startsWith("lwc ") || message.equalsIgnoreCase("lock") || 
				message.startsWith("lock ") || message.equalsIgnoreCase("cmodify") || message.startsWith("cmodify ")) {
			e.getPlayer().sendMessage(tc3 + "Tuntematon komento! Etsitkö kenties komentoa " + tc4 + "/lukko" + tc3 + "?");
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerQuit
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Protection.getLockManager().getBypasses().remove(e.getPlayer().getName());
		Protection.getLockCommand().getLockActions().remove(e.getPlayer().getName());
		Protection.getLockCommand().getMultiLockActions().remove(e.getPlayer().getName());
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerInteract
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			
			Block b = e.getClickedBlock();
			
			boolean bool = false;
			boolean door = false;
			String type = "";
			String type2 = "";
			String type3 = "";
			String type4 = "";
			String type5 = "";
			if (b.getType().toString().contains("_DOOR") && !b.getType().toString().contains("IRON_DOOR")) {
				bool = true;
				door = true;
				type = "ovi";
				type2 = "oven";
				type3 = "ovia";
				type4 = "ovea";
				type5 = "oviin";
			}
			else if (b.getType().toString().contains("_FENCE_GATE")) {
				bool = true;
				type = "portti";
				type2 = "portin";
				type3 = "portteja";
				type4 = "porttia";
				type5 = "portteihin";
			}
			else if (b.getType().toString().contains("_TRAPDOOR")) {
				bool = true;
				type = "ansaluukku";
				type2 = "ansaluukun";
				type3 = "ansaluukkuja";
				type4 = "ansaluukkua";
				type5 = "ansaluukkuihin";
			}
			else if (b.getType().toString().contains("SHULKER_BOX")) {
				bool = true;
				type = "shulker-laatikko";
				type2 = "shulker-laatikon";
				type3 = "shulker-laatikkoja";
				type4 = "shulker-laatikkoa";
				type5 = "shulker-laatikoihin";
			}
			else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				bool = true;
				type = "arkku";
				type2 = "arkun";
				type3 = "arkkuja";
				type4 = "arkkua";
				type5 = "arkkuihin";
			}
			else if (b.getType() == Material.FURNACE) {
				bool = true;
				type = "uuni";
				type2 = "uunin";
				type3 = "uuneja";
				type4 = "uunia";
				type5 = "uuneihin";
			}
			else if (b.getType() == Material.DISPENSER) {
				bool = true;
				type = "jakelulaite";
				type2 = "jakelulaitteen";
				type3 = "jakelulaitteita";
				type4 = "jakelulaittta";
				type5 = "jakelulaitteisiin";
			}
			else if (b.getType() == Material.DROPPER) {
				bool = true;
				type = "pudottaja";
				type2 = "pudottajan";
				type3 = "pudottajia";
				type4 = "pudottajaa";
				type5 = "pudottajiin";
			}
			else if (b.getType() == Material.HOPPER) {
				bool = true;
				type = "suppilo";
				type2 = "suppilon";
				type3 = "suppiloita";
				type4 = "suppiloa";
				type5 = "suppiloihin";
			}
			else if (b.getType() == Material.ANVIL || b.getType() == Material.CHIPPED_ANVIL || b.getType() == Material.DAMAGED_ANVIL) {
				bool = true;
				type = "alasin";
				type2 = "alasimen";
				type3 = "alasimia";
				type4 = "alasinta";
				type5 = "alasimiin";
			}
			else if (b.getType() == Material.BREWING_STAND) {
				bool = true;
				type = "hautumateline";
				type2 = "hautumatelineen";
				type3 = "hautumatelineitä";
				type4 = "hautumatelinettä";
				type5 = "hautumatelineisiin";
			}
			else if (b.getType() == Material.JUKEBOX) {
				bool = true;
				type = "levysoitin";
				type2 = "levysoittimen";
				type3 = "levysoittimia";
				type4 = "levysoitinta";
				type5 = "levysoittimiin";
			}
			else if (b.getType() == Material.SMOKER) {
				bool = true;
				type = "savustin";
				type2 = "savustimen";
				type3 = "savustimia";
				type4 = "savustinta";
				type5 = "savustimiin";
			}
			else if (b.getType() == Material.BLAST_FURNACE) {
				bool = true;
				type = "masuuni";
				type2 = "masuunin";
				type3 = "masuuneja";
				type4 = "masuunia";
				type5 = "masuuneihin";
			}
			else if (b.getType() == Material.BARREL) {
				bool = true;
				type = "tynnyri";
				type2 = "tynnyrin";
				type3 = "tynnyreitä";
				type4 = "tynnyriä";
				type5 = "tynnyreihin";
			}
			
			boolean isLockAction = Protection.getLockCommand().getLockActions().containsKey(e.getPlayer().getName());
			boolean isMultiLockAction = Protection.getLockCommand().getMultiLockActions().containsKey(e.getPlayer().getName());
			
			if (isLockAction || isMultiLockAction) {
				String[] data = null;
				if (isLockAction) {
					data = Protection.getLockCommand().getLockActions().get(e.getPlayer().getName()).split(" ");
				}
				else {
					if (e.getHand() != EquipmentSlot.HAND) {
						e.setCancelled(true);
						return;
					}
					data = Protection.getLockCommand().getMultiLockActions().get(e.getPlayer().getName()).split(" ");
				}
				if (data.length >= 1) {
					if (data[0].equalsIgnoreCase("lukitse")) {
						if (bool) {
							if (door) {
								Door d = (Door) b.getBlockData();
								if (d.getHalf() == Half.TOP) {
									Protection.getLockCommand().lukitse(e.getPlayer(), b.getRelative(BlockFace.DOWN), type, type2, type3, tc2, tc3);
									if (isMultiLockAction) {
										new BukkitRunnable() {
											public void run() {
												e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
											}
										}.runTaskLater(Protection.getPlugin(), 25);
									}
								}
								else {
									Protection.getLockCommand().lukitse(e.getPlayer(), b, type, type2, type3, tc2, tc3);
									if (isMultiLockAction) {
										new BukkitRunnable() {
											public void run() {
												e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
											}
										}.runTaskLater(Protection.getPlugin(), 25);
									}
								}
							}
							else {
								Protection.getLockCommand().lukitse(e.getPlayer(), b, type, type2, type3, tc2, tc3);
								if (isMultiLockAction) {
									new BukkitRunnable() {
										public void run() {
											e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
										}
									}.runTaskLater(Protection.getPlugin(), 25);
								}
							}
						}
						else {
							e.getPlayer().sendMessage(tc3 + "Tätä palikkaa ei voi lukita!");
							e.getPlayer().sendTitle("§4§l✖", "", 5, 15, 5);
							e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
							if (isMultiLockAction) {
								new BukkitRunnable() {
									public void run() {
										e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
									}
								}.runTaskLater(Protection.getPlugin(), 25);
							}
						}
					}
					else if (data[0].equalsIgnoreCase("avaa")) {
						if (bool) {
							if (door) {
								Door d = (Door) b.getBlockData();
								if (d.getHalf() == Half.TOP) {
									Protection.getLockCommand().avaa(e.getPlayer(), b.getRelative(BlockFace.DOWN), type, type2, tc2, tc3);
									if (isMultiLockAction) {
										new BukkitRunnable() {
											public void run() {
												e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
											}
										}.runTaskLater(Protection.getPlugin(), 25);
									}
								}
								else {
									Protection.getLockCommand().avaa(e.getPlayer(), b, type, type2, tc2, tc3);
									if (isMultiLockAction) {
										new BukkitRunnable() {
											public void run() {
												e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
											}
										}.runTaskLater(Protection.getPlugin(), 25);
									}
								}
							}
							else {
								Protection.getLockCommand().avaa(e.getPlayer(), b, type, type2, tc2, tc3);
								if (isMultiLockAction) {
									new BukkitRunnable() {
										public void run() {
											e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
										}
									}.runTaskLater(Protection.getPlugin(), 25);
								}
							}
						}
						else {
							e.getPlayer().sendMessage(tc3 + "Tämä palikka ei ole lukossa!");
							e.getPlayer().sendTitle("§4§l✖", "", 5, 15, 5);
							e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
							if (isMultiLockAction) {
								new BukkitRunnable() {
									public void run() {
										e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
									}
								}.runTaskLater(Protection.getPlugin(), 25);
							}
						}
					}
					else if (data[0].equalsIgnoreCase("oikeudet")) {
						if (data.length >= 2) {
							String[] args = new String[data.length - 1];
							for (int i = 1; i < data.length; i++) {
								args[i - 1] = data[i];
							}
							if (bool) {
								if (door) {
									Door d = (Door) b.getBlockData();
									if (d.getHalf() == Half.TOP) {
										Protection.getLockCommand().oikeudet(e.getPlayer(), b.getRelative(BlockFace.DOWN), args, type, type4, type2, type5, tc1, tc2, tc3);
										if (isMultiLockAction) {
											new BukkitRunnable() {
												public void run() {
													e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
												}
											}.runTaskLater(Protection.getPlugin(), 25);
										}
									}
									else {
										Protection.getLockCommand().oikeudet(e.getPlayer(), b, args, type, type4, type2, type5, tc1, tc2, tc3);
										if (isMultiLockAction) {
											new BukkitRunnable() {
												public void run() {
													e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
												}
											}.runTaskLater(Protection.getPlugin(), 25);
										}
									}
								}
								else {
									Protection.getLockCommand().oikeudet(e.getPlayer(), b, args, type, type4, type2, type5, tc1, tc2, tc3);
									if (isMultiLockAction) {
										new BukkitRunnable() {
											public void run() {
												e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
											}
										}.runTaskLater(Protection.getPlugin(), 25);
									}
								}
							}
							else {
								e.getPlayer().sendMessage(tc3 + "Tämä palikka ei ole lukossa!");
								e.getPlayer().sendTitle("§4§l✖", "", 5, 15, 5);
								e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
								if (isMultiLockAction) {
									new BukkitRunnable() {
										public void run() {
											e.getPlayer().sendTitle("", "§fKlikkaa haluamiasi kohteita...", 0, 200, 20);
										}
									}.runTaskLater(Protection.getPlugin(), 25);
								}
							}
						}
					}
					e.setCancelled(true);
					Protection.getLockCommand().getLockActions().remove(e.getPlayer().getName());
					return;
				}
			}
			
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && bool && Protection.getLockManager().isLocked(b)) {
				if (!Protection.getLockManager().hasPermission(b, e.getPlayer())) {
					e.setCancelled(true);
					Area area = Protection.getAreaManager().getArea(b.getLocation());
					if (area == null || !area.hasFlag(Flag.HIDE_LOCK_MESSAGES)) {
						e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
						e.getPlayer().sendTitle("§4§l✖", "§cTämä " + type + " on lukittu...", 10, 15, 5);
					}
				}
				else {
					if (e.getPlayer().isSneaking() && SettingsUtils.getSetting(e.getPlayer(), "show_lock_info")) {
						e.setCancelled(true);
						String kohde = type;
						new BukkitRunnable() {
							public void run() {
								e.getPlayer().sendMessage("");
								e.getPlayer().sendMessage(tc2 + "§m-----------" + tc1 + " Lukittu " + kohde + " " + tc2 + "§m-----------");
								e.getPlayer().sendMessage("");
								e.getPlayer().sendMessage(tc2 + " Omistaja: " + tc1 + Protection.getLockManager().getOwnerName(b));
								e.getPlayer().sendMessage("");
								e.getPlayer().sendMessage(tc2 + " Oikeudet:");
								e.getPlayer().sendMessage("");
								boolean bo = false;
								for (String s : Protection.getLockManager().getPermissions(b)) {
									if (s.equals("^")) {
										continue;
									}
									else if (s.equals("*")) {
										e.getPlayer().sendMessage(tc2 + "  - " + tc1 + "Kaikki ✦");
										bo = true;
									}
									else if (s.equals("+")) {
										e.getPlayer().sendMessage(tc2 + "  - " + tc1 + "Punakivi §l∞");
										bo = true;
									}
									else if (s.equals("_")) {
										e.getPlayer().sendMessage(tc2 + "  - " + tc1 + "Suppilot §l▼");
										bo = true;
									}
									else {
										MySQLResult infoData = MySQLUtils.get("SELECT name FROM player_info WHERE uuid=?", s);
										if (infoData != null) {
											e.getPlayer().sendMessage(tc2 + "  - " + tc1 + infoData.getString(0, "name"));
											bo = true;
										}
									}
								}
								if (!bo) {
									e.getPlayer().sendMessage(tc2 + "  - " + tc3 + "Vain sinä");
								}
								e.getPlayer().sendMessage("");
							}
						}.runTaskAsynchronously(Protection.getPlugin());
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockBreak
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		
		Block b = e.getBlock();
		
		if (Protection.getLockManager().isLocked(b)) {
			String type = "palikka";
			if (b.getType().toString().contains("_DOOR") && !b.getType().toString().contains("IRON_DOOR")) {
				type = "ovi";
			}
			else if (b.getType().toString().contains("_FENCE_GATE")) {
				type = "portti";
			}
			else if (b.getType().toString().contains("_TRAPDOOR")) {
				type = "ansaluukku";
			}
			else if (b.getType().toString().contains("SHULKER_BOX")) {
				type = "shulker-laatikko";
			}
			else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				type = "arkku";
			}
			else if (b.getType() == Material.FURNACE) {
				type = "uuni";
			}
			else if (b.getType() == Material.DISPENSER) {
				type = "jakelulaite";
			}
			else if (b.getType() == Material.DROPPER) {
				type = "pudottaja";
			}
			else if (b.getType() == Material.HOPPER) {
				type = "suppilo";
			}
			else if (b.getType() == Material.ANVIL || b.getType() == Material.CHIPPED_ANVIL || b.getType() == Material.DAMAGED_ANVIL) {
				type = "alasin";
			}
			else if (b.getType() == Material.BREWING_STAND) {
				type = "hautumateline";
			}
			else if (b.getType() == Material.JUKEBOX) {
				type = "levysoitin";
			}
			else if (b.getType() == Material.SMOKER) {
				type = "savustin";
			}
			else if (b.getType() == Material.BLAST_FURNACE) {
				type = "masuuni";
			}
			else if (b.getType() == Material.BARREL) {
				type = "tynnyri";
			}
			e.setCancelled(true);
			Area area = Protection.getAreaManager().getArea(b.getLocation());
			if (area == null || !area.hasFlag(Flag.HIDE_LOCK_MESSAGES)) {
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
				e.getPlayer().sendTitle("§4§l✖", "§cTämä " + type + " on lukittu...", 10, 15, 5);
				if (Protection.getLockManager().isOwner(b, e.getPlayer())) {
					ReflectionUtils.sendChatPacket(e.getPlayer(), "{\"text\":\"§cTämä §4" + type + "§c on lukittu, joten sitä ei voi hajottaa! Poista lukitus ensin komennolla §4/avaa§c!\"}", ChatMessageType.ACTION_BAR);
				}
			}
		}
		else {
			Block above = b.getRelative(BlockFace.UP);
			if (above != null && above.getType().toString().contains("_DOOR") && !above.getType().toString().contains("IRON_DOOR")) {
				if (Protection.getLockManager().isLocked(above)) {
					e.setCancelled(true);
					Area area = Protection.getAreaManager().getArea(above.getLocation());
					if (area == null || !area.hasFlag(Flag.HIDE_LOCK_MESSAGES)) {
						e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
						e.getPlayer().sendTitle("§4§l✖", "§cTämä ovi on lukittu...", 10, 15, 5);
						if (Protection.getLockManager().isOwner(b, e.getPlayer())) {
							ReflectionUtils.sendChatPacket(e.getPlayer(), "{\"text\":\"§cTämä §4ovi§c on lukittu, joten sitä ei voi hajottaa! Poista lukitus ensin komennolla §4/avaa§c!\"}", ChatMessageType.ACTION_BAR);
						}
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockRedstone
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent e) {
		Block b = e.getBlock();
		if (b.getType() == Material.OAK_TRAPDOOR || b.getType() == Material.BIRCH_TRAPDOOR || b.getType() == Material.SPRUCE_TRAPDOOR || b.getType() == Material.JUNGLE_TRAPDOOR || 
				b.getType() == Material.ACACIA_TRAPDOOR || b.getType() == Material.DARK_OAK_TRAPDOOR || b.getType() == Material.OAK_DOOR || b.getType() == Material.BIRCH_DOOR || 
				b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR || 
				b.getType() == Material.OAK_FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || 
				b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
			if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().getPermissions(b).contains("*") && !Protection.getLockManager().getPermissions(b).contains("+")) {
				e.setNewCurrent(e.getOldCurrent());
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onChestHopper
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onChestHopper(InventoryMoveItemEvent e) {
		if (e.getDestination().getType() == InventoryType.HOPPER && (e.getSource().getType() == InventoryType.CHEST || e.getSource().getType() == InventoryType.BARREL || 
				e.getSource().getType() == InventoryType.BLAST_FURNACE || e.getSource().getType() == InventoryType.BREWING || e.getSource().getType() == InventoryType.DISPENSER || 
				e.getSource().getType() == InventoryType.DROPPER || e.getSource().getType() == InventoryType.FURNACE || e.getSource().getType() == InventoryType.HOPPER || 
				e.getSource().getType() == InventoryType.SHULKER_BOX || e.getSource().getType() == InventoryType.SMOKER)) {
			Location l = e.getSource().getLocation();
			if (l != null) {
				Block b = l.getBlock();
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().getPermissions(b).contains("_") && !Protection.getLockManager().getPermissions(b).contains("*")) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockPistonExtend
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent e) {
		for (Block b : e.getBlocks()) {
			if (Protection.getLockManager().isLocked(b)) {
				e.setCancelled(true);
				return;
			}
			else {
				Block above = b.getRelative(BlockFace.UP);
				if (above != null && above.getType().toString().contains("_DOOR") && !above.getType().toString().contains("IRON_DOOR")) {
					if (Protection.getLockManager().isLocked(above)) {
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockPistonRetract
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent e) {
		for (Block b : e.getBlocks()) {
			if (Protection.getLockManager().isLocked(b)) {
				e.setCancelled(true);
				return;
			}
			else {
				Block above = b.getRelative(BlockFace.UP);
				if (above != null && above.getType().toString().contains("_DOOR") && !above.getType().toString().contains("IRON_DOOR")) {
					if (Protection.getLockManager().isLocked(above)) {
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onEntityExplode
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		Iterator<Block> iterator = e.blockList().iterator();
		while (iterator.hasNext()) {
			Block b = iterator.next();
			if (Protection.getLockManager().isLocked(b)) {
				iterator.remove();
			}
			else {
				Block above = b.getRelative(BlockFace.UP);
				if (above != null && above.getType().toString().contains("_DOOR") && !above.getType().toString().contains("IRON_DOOR")) {
					if (Protection.getLockManager().isLocked(above)) {
						iterator.remove();
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockExplode
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		Iterator<Block> iterator = e.blockList().iterator();
		while (iterator.hasNext()) {
			Block b = iterator.next();
			if (Protection.getLockManager().isLocked(b)) {
				iterator.remove();
			}
			else {
				Block above = b.getRelative(BlockFace.UP);
				if (above != null && above.getType().toString().contains("_DOOR") && !above.getType().toString().contains("IRON_DOOR")) {
					if (Protection.getLockManager().isLocked(above)) {
						iterator.remove();
					}
				}
			}
		}
	}
}