package me.t4tu.rkprotection.locks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import me.t4tu.rkprotection.Protection;

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
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = e.getClickedBlock();
			boolean cancel = false;
			String type = "";
			if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR) {
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().hasPermission(b, e.getPlayer())) {
					cancel = true;
					type = "ovi";
				}
			}
			else if (b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().hasPermission(b, e.getPlayer())) {
					cancel = true;
					type = "portti";
				}
			}
			else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().hasPermission(b, e.getPlayer())) {
					cancel = true;
					type = "arkku";
				}
			}
			else if (b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE) {
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().hasPermission(b, e.getPlayer())) {
					cancel = true;
					type = "uuni";
				}
			}
			else if (b.getType() == Material.TRAP_DOOR) {
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().hasPermission(b, e.getPlayer())) {
					cancel = true;
					type = "ansaluukku";
				}
			}
			if (cancel) {
				e.setCancelled(true);
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
				e.getPlayer().sendTitle("§4§l✖", "§cTämä " + type + " on lukittu...", 10, 15, 5);
			}
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = e.getClickedBlock();
			if (e.getPlayer().isSneaking()) {
				boolean show = false;
				String type = "";
				if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR) {
					if (Protection.getLockManager().isLocked(b) && Protection.getLockManager().hasPermission(b, e.getPlayer())) {
						show = true;
						type = "ovi";
					}
				}
				else if (b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
					if (Protection.getLockManager().isLocked(b) && Protection.getLockManager().hasPermission(b, e.getPlayer())) {
						show = true;
						type = "portti";
					}
				}
				else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
					if (Protection.getLockManager().isLocked(b) && Protection.getLockManager().hasPermission(b, e.getPlayer())) {
						show = true;
						type = "arkku";
					}
				}
				else if (b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE) {
					if (Protection.getLockManager().isLocked(b) && Protection.getLockManager().hasPermission(b, e.getPlayer())) {
						show = true;
						type = "uuni";
					}
				}
				else if (b.getType() == Material.TRAP_DOOR) {
					if (Protection.getLockManager().isLocked(b) && Protection.getLockManager().hasPermission(b, e.getPlayer())) {
						show = true;
						type = "ansaluukku";
					}
				}
				if (show && SettingsUtils.getSetting(e.getPlayer(), "show_lock_info")) {
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
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockBreak
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		boolean cancel = false;
		String type = "";
		if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR) {
			if (Protection.getLockManager().isLocked(b)) {
				cancel = true;
				type = "ovi";
			}
		}
		else if (b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
			if (Protection.getLockManager().isLocked(b)) {
				cancel = true;
				type = "portti";
			}
		}
		else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
			if (Protection.getLockManager().isLocked(b)) {
				cancel = true;
				type = "arkku";
			}
		}
		else if (b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE) {
			if (Protection.getLockManager().isLocked(b)) {
				cancel = true;
				type = "uuni";
			}
		}
		else if (b.getType() == Material.TRAP_DOOR) {
			if (Protection.getLockManager().isLocked(b)) {
				cancel = true;
				type = "ansaluukku";
			}
		}
		if (cancel) {
			e.setCancelled(true);
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
			e.getPlayer().sendTitle("§4§l✖", "§cTämä " + type + " on lukittu...", 10, 15, 5);
			if (Protection.getLockManager().isOwner(b, e.getPlayer())) {
				ReflectionUtils.sendChatPacket(e.getPlayer(), "{\"text\":\"§cTämä §4" + type + "§c on lukittu, joten sitä ei voi hajottaa! Poista lukitus ensin komennolla §4/avaa§c!\"}", ChatMessageType.ACTION_BAR);
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
		if (b.getType() == Material.TRAP_DOOR || b.getType() == Material.WOODEN_DOOR || b.getType() == Material.BIRCH_DOOR || b.getType() == Material.SPRUCE_DOOR || b.getType() == Material.JUNGLE_DOOR || b.getType() == Material.ACACIA_DOOR || b.getType() == Material.DARK_OAK_DOOR || 
				b.getType() == Material.FENCE_GATE || b.getType() == Material.BIRCH_FENCE_GATE || b.getType() == Material.SPRUCE_FENCE_GATE || b.getType() == Material.JUNGLE_FENCE_GATE || b.getType() == Material.ACACIA_FENCE_GATE || b.getType() == Material.DARK_OAK_FENCE_GATE) {
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
		if (e.getDestination().getType() == InventoryType.HOPPER && e.getSource().getType() == InventoryType.CHEST) {
			Location l = e.getSource().getLocation();
			if (l != null) {
				Block b = l.getBlock();
				if (Protection.getLockManager().isLocked(b) && !Protection.getLockManager().getPermissions(b).contains("_") && !Protection.getLockManager().getPermissions(b).contains("*")) {
					e.setCancelled(true);
				}
			}
		}
	}
}