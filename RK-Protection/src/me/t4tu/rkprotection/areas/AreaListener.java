package me.t4tu.rkprotection.areas;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkprotection.Protection;

public class AreaListener implements Listener {
	
	private static final List<SpawnReason> REASONS = Arrays.asList(SpawnReason.CHUNK_GEN, SpawnReason.NATURAL, SpawnReason.ENDER_PEARL, SpawnReason.DISPENSE_EGG, SpawnReason.SLIME_SPLIT, 
			SpawnReason.LIGHTNING, SpawnReason.JOCKEY, SpawnReason.REINFORCEMENTS, SpawnReason.SPAWNER, SpawnReason.SILVERFISH_BLOCK, SpawnReason.MOUNT, SpawnReason.EGG);
	private static final List<Material> CONTAINERS = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.WORKBENCH, Material.FURNACE, Material.ANVIL, 
			Material.ENCHANTMENT_TABLE, Material.JUKEBOX, Material.DISPENSER, Material.DROPPER, Material.HOPPER);
	private static final List<Material> DOORS = Arrays.asList(Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, 
			Material.IRON_DOOR_BLOCK, Material.TRAP_DOOR, Material.IRON_TRAPDOOR, Material.FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE, 
			Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE);
	private static final List<Material> REDSTONE = Arrays.asList(Material.WOOD_BUTTON, Material.STONE_BUTTON, Material.LEVER, Material.WOOD_PLATE, Material.STONE_PLATE, Material.GOLD_PLATE, Material.IRON_PLATE);
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo().getBlock().getLocation().distance(e.getFrom().getBlock().getLocation()) > 0) {
			
			Area from = Protection.getAreaManager().getArea(e.getFrom());
			Area to = Protection.getAreaManager().getArea(e.getTo());
			
			if (to != from) {
				if (to != null) {
					if (!to.hasFlag(Flag.HIDE_MESSAGES)) {
						e.getPlayer().sendMessage("§2Saavut alueelle: §a" + to.getName().replace("_", " "));
						if (to.hasFlag(Flag.PVP) && to.hasFlag(Flag.SHOW_PVP_MESSAGE)) {
							e.getPlayer().sendMessage("");
							e.getPlayer().sendMessage("§4§l Varoitus!");
							e.getPlayer().sendMessage("");
							e.getPlayer().sendMessage("§c Saavut PvP-alueelle! Tällä alueella muut voivat tappaa sinut ja sinä voit tappaa muita.");
							e.getPlayer().sendMessage("");
						}
					}
					else {
						if (from != null && !from.hasFlag(Flag.HIDE_MESSAGES)) {
							e.getPlayer().sendMessage("§2Poistut alueelta: §a" + from.getName().replace("_", " "));
						}
					}
				}
				else {
					if (!from.hasFlag(Flag.HIDE_MESSAGES)) {
						e.getPlayer().sendMessage("§2Poistut alueelta: §a" + from.getName().replace("_", " "));
					}
				}
			}
			
			if (to == null && from != null && from.hasFlag(Flag.SHOW_PROTECTED_MESSAGE)) {
				e.getPlayer().sendMessage("§2Poistut suojatulta alueelta. Voit nyt rakentaa sääntöjen rajoissa.");
			}
			else if (to != null && from == null && to.hasFlag(Flag.SHOW_PROTECTED_MESSAGE)) {
				e.getPlayer().sendMessage("§2Saavut suojatulle alueelle. Et voi rakentaa täällä.");
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Area victimArea = Protection.getAreaManager().getArea(e.getEntity().getLocation());
		Area damagerArea = Protection.getAreaManager().getArea(e.getDamager().getLocation());
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (Protection.getAreaManager().getPvPBypasses().contains(e.getDamager().getName())) {
				return;
			}
			if (victimArea != null) {
				if (!victimArea.hasFlag(Flag.PVP)) {
					e.setCancelled(true);
				}
			}
			else {
				e.setCancelled(true);
			}
			if (damagerArea != null) {
				if (!damagerArea.hasFlag(Flag.PVP)) {
					e.setCancelled(true);
				}
			}
			else {
				e.setCancelled(true);
			}
		}
		if (e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Painting || e.getEntity() instanceof ArmorStand) {
			if (!CoreUtils.getAdminPowers().contains(e.getDamager().getName()) && !CoreUtils.getBuilderPowers().contains(e.getDamager().getName()) && !e.getDamager().isOp()) {
				if (victimArea != null) {
					if (!victimArea.hasFlag(Flag.ALLOW_DESTROYING)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof ItemFrame || e.getRightClicked() instanceof Painting || e.getRightClicked() instanceof ArmorStand) {
			if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
				Area area = Protection.getAreaManager().getArea(e.getRightClicked().getLocation());
				if (area != null) {
					if (!area.hasFlag(Flag.ALLOW_MISC_INTERACTION)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.PHYSICAL) {
			if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
				Area area = Protection.getAreaManager().getArea(e.getClickedBlock().getLocation());
				if (area != null) {
					if (!area.hasFlag(Flag.ALLOW_BUILDING)) {
						if (e.getHand() == EquipmentSlot.HAND && CoreUtils.isNotAir(e.getPlayer().getInventory().getItemInMainHand())) {
							Material material = e.getPlayer().getInventory().getItemInMainHand().getType();
							if (material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET || material == Material.ARMOR_STAND 
									|| material == Material.ITEM_FRAME || material == Material.PAINTING) {
								e.setCancelled(true);
								e.getPlayer().updateInventory();
								return;
							}
						}
						if (e.getHand() == EquipmentSlot.OFF_HAND && CoreUtils.isNotAir(e.getPlayer().getInventory().getItemInOffHand())) {
							Material material = e.getPlayer().getInventory().getItemInOffHand().getType();
							if (material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET || material == Material.ARMOR_STAND 
									|| material == Material.ITEM_FRAME || material == Material.PAINTING) {
								e.setCancelled(true);
								e.getPlayer().updateInventory();
								return;
							}
						}
						
					}
					if (area.hasFlag(Flag.ALLOW_BLOCK_INTERACTION)) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_CONTAINERS) && CONTAINERS.contains(e.getClickedBlock().getType())) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_DOORS) && DOORS.contains(e.getClickedBlock().getType())) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_REDSTONE) && REDSTONE.contains(e.getClickedBlock().getType())) {
						return;
					}
					e.setCancelled(true);
				}
			}
		}
		if (e.getAction() == Action.PHYSICAL) {
			Area area = Protection.getAreaManager().getArea(e.getClickedBlock().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_DESTROYING)) {
					if (e.getClickedBlock().getType() == Material.SOIL) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
			Area area = Protection.getAreaManager().getArea(e.getBlock().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_BUILDING)) {
					e.setCancelled(true);
					e.getPlayer().updateInventory();
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
			Area area = Protection.getAreaManager().getArea(e.getBlock().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_DESTROYING)) {
					e.setCancelled(true);
					e.getPlayer().updateInventory();
				}
			}
		}
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getLocation());
		if (area != null) {
			EntityType t = e.getEntityType();
			if ((t == EntityType.BLAZE || t == EntityType.CAVE_SPIDER || t == EntityType.CREEPER || t == EntityType.ENDERMAN || 
					t == EntityType.ENDERMITE || t == EntityType.GHAST || t == EntityType.GUARDIAN || t == EntityType.MAGMA_CUBE || 
					t == EntityType.PIG_ZOMBIE || t == EntityType.SHULKER_BULLET || t == EntityType.SILVERFISH || t == EntityType.SKELETON || 
					t == EntityType.SLIME || t == EntityType.SPIDER || t == EntityType.WITCH || t == EntityType.ZOMBIE || t == EntityType.ZOMBIE_VILLAGER || 
					t == EntityType.VEX || t == EntityType.EVOKER || t == EntityType.HUSK || t == EntityType.STRAY) && 
					area.hasFlag(Flag.NO_MOBS) && REASONS.contains(e.getSpawnReason())) {
				e.setCancelled(true);
			}
			else if ((t == EntityType.CHICKEN || t == EntityType.COW || t == EntityType.DONKEY || t == EntityType.HORSE || 
					t == EntityType.LLAMA || t == EntityType.MULE || t == EntityType.OCELOT || t == EntityType.PIG || 
					t == EntityType.POLAR_BEAR || t == EntityType.RABBIT || t == EntityType.SHEEP || t == EntityType.WOLF|| 
					t == EntityType.SQUID) && area.hasFlag(Flag.NO_ANIMALS) && REASONS.contains(e.getSpawnReason())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onHangingPlace(HangingPlaceEvent e) {
		if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
			Area area = Protection.getAreaManager().getArea(e.getEntity().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_BUILDING)) {
					e.setCancelled(true);
					e.getPlayer().updateInventory();
				}
			}
		}
	}
	
	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
		if (!CoreUtils.getAdminPowers().contains(e.getRemover().getName()) && !CoreUtils.getBuilderPowers().contains(e.getRemover().getName()) && !e.getRemover().isOp()) {
			Area area = Protection.getAreaManager().getArea(e.getEntity().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_DESTROYING)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakEvent e) {
		if (e.getCause() == RemoveCause.EXPLOSION || e.getCause() == RemoveCause.OBSTRUCTION || e.getCause() == RemoveCause.PHYSICS) {
			Area area = Protection.getAreaManager().getArea(e.getEntity().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_DESTROYING)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
			Area area = Protection.getAreaManager().getArea(e.getRightClicked().getLocation());
			if (area != null) {
				if (!area.hasFlag(Flag.ALLOW_MISC_INTERACTION)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerFish(PlayerFishEvent e) {
		if (e.getState() == State.CAUGHT_ENTITY) {
			if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
				Area area = Protection.getAreaManager().getArea(e.getCaught().getLocation());
				if (area != null) {
					if (!area.hasFlag(Flag.ALLOW_MISC_INTERACTION)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getEntity().getLocation());
		if (area != null) {
			if (!area.hasFlag(Flag.ALLOW_DESTROYING)) {
				e.setCancelled(true);
			}
		}
	}
}