package me.t4tu.rkprotection.areas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.t4tu.rkcore.statistics.PlayerStatisticsEntry;
import me.t4tu.rkcore.statistics.Statistic;
import me.t4tu.rkcore.statistics.StatisticsManager;
import me.t4tu.rkcore.statistics.StatisticsViewer;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkprotection.Protection;
import net.md_5.bungee.api.ChatColor;

public class AreaListener implements Listener {
	
	private static final List<SpawnReason> REASONS = Arrays.asList(SpawnReason.NATURAL, SpawnReason.ENDER_PEARL, SpawnReason.DISPENSE_EGG, SpawnReason.SLIME_SPLIT, SpawnReason.LIGHTNING, 
			SpawnReason.JOCKEY, SpawnReason.REINFORCEMENTS, SpawnReason.SPAWNER, SpawnReason.SPAWNER_EGG, SpawnReason.BUILD_IRONGOLEM, SpawnReason.BUILD_SNOWMAN, SpawnReason.BUILD_WITHER, 
			SpawnReason.VILLAGE_INVASION, SpawnReason.VILLAGE_DEFENSE, SpawnReason.SILVERFISH_BLOCK, SpawnReason.MOUNT, SpawnReason.EGG, SpawnReason.DEFAULT);
	private static final List<Material> CONTAINERS = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.CRAFTING_TABLE, Material.FURNACE, Material.ANVIL, 
			Material.ENCHANTING_TABLE, Material.JUKEBOX, Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.BREWING_STAND, Material.BARREL, Material.BLAST_FURNACE, 
			Material.CARTOGRAPHY_TABLE, Material.COMPOSTER, Material.GRINDSTONE, Material.LECTERN, Material.LOOM, Material.SMOKER, Material.STONECUTTER, Material.FLETCHING_TABLE, 
			Material.SMITHING_TABLE);
	
	private List<Location> fireSpreadCooldown = new ArrayList<Location>();
	private List<String> denyMessageCooldown = new ArrayList<String>();
	
	public List<Location> getFireSpreadCooldown() {
		return fireSpreadCooldown;
	}
	
	public List<String> getDenyMessageCooldown() {
		return denyMessageCooldown;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo().getBlock().getLocation().distance(e.getFrom().getBlock().getLocation()) > 0) {
			
			Area from = Protection.getAreaManager().getArea(e.getFrom());
			Area to = Protection.getAreaManager().getArea(e.getTo());
			
			if (to != from) {
				if (to != null) {
					if (to.hasFlag(Flag.DENY_ENTRY)) {
						e.setCancelled(true);
						if (!denyMessageCooldown.contains(e.getPlayer().getName())) {
							e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', to.getDenyMessage()));
							denyMessageCooldown.add(e.getPlayer().getName());
							new BukkitRunnable() {
								public void run() {
									denyMessageCooldown.remove(e.getPlayer().getName());
								}
							}.runTaskLater(Protection.getPlugin(), 20);
						}
						return;
					}
					if (!to.hasFlag(Flag.HIDE_MESSAGES)) {
						if (to.hasFlag(Flag.SHOW_PVP_MESSAGE)) {
							e.getPlayer().sendMessage("§cSaavut PvP-alueelle!");
						}
						else {
							e.getPlayer().sendMessage("§3Saavut alueelle: §b" + to.getName().replace("_", " "));
						}
					}
					else {
						if (from != null && !from.hasFlag(Flag.HIDE_MESSAGES)) {
							if (from.hasFlag(Flag.SHOW_PVP_MESSAGE)) {
								e.getPlayer().sendMessage("§cPoistut PvP-alueelta!");
							}
							else {
								e.getPlayer().sendMessage("§3Poistut alueelta: §b" + from.getName().replace("_", " "));
							}
						}
					}
				}
				else {
					if (!from.hasFlag(Flag.HIDE_MESSAGES)) {
						if (from.hasFlag(Flag.SHOW_PVP_MESSAGE)) {
							e.getPlayer().sendMessage("§cPoistut PvP-alueelta!");
						}
						else {
							e.getPlayer().sendMessage("§3Poistut alueelta: §b" + from.getName().replace("_", " "));
						}
					}
				}
			}
			
			if (to == null && from != null && from.hasFlag(Flag.SHOW_PROTECTED_MESSAGE)) {
				e.getPlayer().sendMessage("§3Poistut suojatulta alueelta. Voit nyt rakentaa sääntöjen rajoissa.");
			}
			else if (to != null && from == null && to.hasFlag(Flag.SHOW_PROTECTED_MESSAGE)) {
				e.getPlayer().sendMessage("§3Saavut suojatulle alueelle. Et voi rakentaa täällä.");
			}
			
			if (to != null && to.hasFlag(Flag.THRONE)) {
				if (!throneMessages.isEmpty()) {
					String message = throneMessages.get(new Random().nextInt(throneMessages.size()));
					e.getPlayer().sendMessage(message);
					e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				}
				Vector vector = new Vector(1, 0.5, 0);
				e.getPlayer().setVelocity(vector);
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
			if (victimArea == null || !victimArea.hasFlag(Flag.PVP)) {
				e.setCancelled(true);
			}
			if (damagerArea == null || !damagerArea.hasFlag(Flag.PVP)) {
				e.setCancelled(true);
			}
		}
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Projectile && !(e.getDamager() instanceof ThrownPotion)) {
			Projectile projectile = (Projectile) e.getDamager();
			if (projectile.getShooter() instanceof Player) {
				Player shooter = (Player) projectile.getShooter();
				Area shooterArea = Protection.getAreaManager().getArea(shooter.getLocation());
				if (Protection.getAreaManager().getPvPBypasses().contains(shooter.getName())) {
					return;
				}
				if (victimArea == null || !victimArea.hasFlag(Flag.PVP)) {
					e.setCancelled(true);
				}
				if (shooterArea == null || !shooterArea.hasFlag(Flag.PVP)) {
					e.setCancelled(true);
				}
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
		if (victimArea != null && victimArea.hasFlag(Flag.DENY_ATTACKING_ENTITIES)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getPlayer().getLocation());
		if (area != null && area.hasFlag(Flag.CUSTOM_RESPAWN_LOCATION)) {
			Location location = area.getRespawnLocation();
			if (location != null) {
				e.setRespawnLocation(location);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getEntity().getLocation());
		if (area != null && area.hasFlag(Flag.KEEP_INVENTORY)) {
			e.setKeepInventory(true);
			e.setKeepLevel(true);
			e.getDrops().clear();
			e.setDroppedExp(0);
		}
		if (area != null && area.hasFlag(Flag.PVP)) {
			StatisticsManager.incrementStatistics(new PlayerStatisticsEntry(Statistic.PVP_DEATHS, 1, e.getEntity().getUniqueId().toString()));
			Player killer = e.getEntity().getKiller();
			if (killer != null && !killer.equals(e.getEntity())) {
				Area killerArea = Protection.getAreaManager().getArea(killer.getLocation());
				if (killerArea != null && killerArea.hasFlag(Flag.PVP)) {
					StatisticsManager.incrementStatistics(new PlayerStatisticsEntry(Statistic.PVP_KILLS, 1, killer.getUniqueId().toString()));
					StatisticsViewer.incrementKillsInPvpTopCache(killer);
					new BukkitRunnable() {
						public void run() {
							StatisticsViewer.updatePvpTopScoreboard(e.getEntity().getWorld());
						}
					}.runTaskAsynchronously(Protection.getPlugin());
				}
			}
		}
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent e) {
		if (e.getPotion().getShooter() instanceof Player) {
			Player player = (Player) e.getPotion().getShooter();
			Area throwerArea = Protection.getAreaManager().getArea(player.getLocation());
			if (Protection.getAreaManager().getPvPBypasses().contains(player.getName())) {
				return;
			}
			boolean b = false;
			for (PotionEffect effect : e.getPotion().getEffects()) {
				if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.HARM)) {
					b = true;
				}
			}
			if (b) {
				for (Player victim : player.getWorld().getPlayers()) {
					if (victim != player) {
						Area victimArea = Protection.getAreaManager().getArea(victim.getLocation());
						if ((victimArea == null || !victimArea.hasFlag(Flag.PVP)) || (throwerArea == null || !throwerArea.hasFlag(Flag.PVP))) {
							e.setIntensity(victim, 0);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent e) {
		PotionType type = e.getEntity().getBasePotionData().getType();
		if (type == PotionType.POISON || type == PotionType.INSTANT_DAMAGE) {
			for (Player victim : e.getEntity().getWorld().getPlayers()) {
				Area victimArea = Protection.getAreaManager().getArea(victim.getLocation());
				if (victimArea == null || !victimArea.hasFlag(Flag.PVP)) {
					e.getAffectedEntities().remove(victim);
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
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
			if (e.getClickedBlock().getType() == Material.CAULDRON) {
				Levelled levelled = (Levelled) e.getClickedBlock().getBlockData();
				if (levelled.getLevel() != 0) {
					Area area = Protection.getAreaManager().getArea(e.getClickedBlock().getLocation());
					if (area != null && area.hasFlag(Flag.INFINITE_WATER_WELLS)) {
						e.setCancelled(true);
						ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
						if (e.getHand() == EquipmentSlot.OFF_HAND) {
							item = e.getPlayer().getInventory().getItemInOffHand();
						}
						if (CoreUtils.isNotAir(item)) {
							if (item.getType() == Material.BUCKET) {
								item.setType(Material.WATER_BUCKET);
								e.getPlayer().updateInventory();
							}
							else if (item.getType() == Material.GLASS_BOTTLE) {
								item.setType(Material.POTION);
								PotionMeta meta = (PotionMeta) item.getItemMeta();
								meta.setBasePotionData(new PotionData(PotionType.WATER));
								item.setItemMeta(meta);
								e.getPlayer().updateInventory();
							}
						}
					}
				}
			}
		}
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.PHYSICAL) {
			if (!CoreUtils.getAdminPowers().contains(e.getPlayer().getName()) && !CoreUtils.getBuilderPowers().contains(e.getPlayer().getName()) && !e.getPlayer().isOp()) {
				Area area = e.getAction() != Action.RIGHT_CLICK_AIR ? Protection.getAreaManager().getArea(e.getClickedBlock().getLocation()) : Protection.getAreaManager().getArea(e.getPlayer().getLocation());
				if (area != null) {
					if (!area.hasFlag(Flag.ALLOW_BUILDING) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (e.getHand() == EquipmentSlot.HAND && CoreUtils.isNotAir(e.getPlayer().getInventory().getItemInMainHand())) {
							Material material = e.getPlayer().getInventory().getItemInMainHand().getType();
							if (material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET || material == Material.ARMOR_STAND 
									|| material == Material.ITEM_FRAME || material == Material.PAINTING) {
								e.setCancelled(true);
								e.getPlayer().updateInventory();
								return;
							}
						}
						else if (e.getHand() == EquipmentSlot.OFF_HAND && CoreUtils.isNotAir(e.getPlayer().getInventory().getItemInOffHand())) {
							Material material = e.getPlayer().getInventory().getItemInOffHand().getType();
							if (material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET || material == Material.ARMOR_STAND 
									|| material == Material.ITEM_FRAME || material == Material.PAINTING) {
								e.setCancelled(true);
								e.getPlayer().updateInventory();
								return;
							}
						}
						
					}
					if (!area.hasFlag(Flag.ALLOW_DESTROYING) && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {
						if (e.getHand() == EquipmentSlot.HAND && CoreUtils.isNotAir(e.getPlayer().getInventory().getItemInMainHand())) {
							Material material = e.getPlayer().getInventory().getItemInMainHand().getType();
							ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
							if ((material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) && ((PotionMeta) meta).getBasePotionData().getType() == PotionType.WATER) {
								e.setCancelled(true);
								e.getPlayer().updateInventory();
								return;
							}
						}
						else if (e.getHand() == EquipmentSlot.OFF_HAND && CoreUtils.isNotAir(e.getPlayer().getInventory().getItemInOffHand())) {
							Material material = e.getPlayer().getInventory().getItemInOffHand().getType();
							ItemMeta meta = e.getPlayer().getInventory().getItemInOffHand().getItemMeta();
							if ((material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) && ((PotionMeta) meta).getBasePotionData().getType() == PotionType.WATER) {
								e.setCancelled(true);
								e.getPlayer().updateInventory();
								return;
							}
						}
					}
					if (!area.hasFlag(Flag.ALLOW_DESTROYING) && e.getAction() == Action.PHYSICAL) {
						if (e.getClickedBlock().getType() == Material.FARMLAND) {
							e.setCancelled(true);
							return;
						}
					}
					if (e.getAction() == Action.RIGHT_CLICK_AIR) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_BLOCK_INTERACTION)) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_CONTAINERS) && CONTAINERS.contains(e.getClickedBlock().getType())) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_DOORS) && e.getClickedBlock().getType().toString().contains("_DOOR")) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_TRAPDOORS) && e.getClickedBlock().getType().toString().contains("_TRAPDOOR")) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_FENCE_GATES) && e.getClickedBlock().getType().toString().contains("_FENCE_GATE")) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_REDSTONE) && (e.getClickedBlock().getType().toString().contains("_BUTTON") || 
							e.getClickedBlock().getType().toString().contains("_PRESSURE_PLATE") || 
							e.getClickedBlock().getType().toString().contains("LEVER"))) {
						return;
					}
					e.setCancelled(true);
				}
			}
			else if (e.getAction() == Action.PHYSICAL) {
				if (e.getClickedBlock().getType() == Material.FARMLAND) {
					Area area = Protection.getAreaManager().getArea(e.getClickedBlock().getLocation());
					if (area != null && !area.hasFlag(Flag.ALLOW_DESTROYING)) {
						e.setCancelled(true);
					}
					return;
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
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getBlock().getLocation());
		if (e.getCause() == IgniteCause.LAVA || e.getCause() == IgniteCause.LIGHTNING) {
			e.setCancelled(true);
			return;
		}
		if (e.getCause() == IgniteCause.SPREAD && (area != null && area.hasFlag(Flag.NO_FIRE_SPREAD))) {
			e.setCancelled(true);
			return;
		}
		if (e.getCause() == IgniteCause.SPREAD && (fireSpreadCooldown.size() > 500 || fireSpreadCooldown.contains(e.getBlock().getLocation()))) {
			e.setCancelled(true);
			return;
		}
		new BukkitRunnable() {
			public void run() {
				if (e.getBlock().getType() == Material.FIRE && e.getBlock().getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK && (area == null || !area.hasFlag(Flag.NO_FIRE_SPREAD))) {
					e.getBlock().setType(Material.AIR);
					if (!fireSpreadCooldown.contains(e.getBlock().getLocation())) {
						fireSpreadCooldown.add(e.getBlock().getLocation());
						new BukkitRunnable() {
							public void run() {
								fireSpreadCooldown.remove(e.getBlock().getLocation());
							}
						}.runTaskLater(Protection.getPlugin(), 2400);
					}
				}
			}
		}.runTaskLater(Protection.getPlugin(), 300);
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockFade(BlockFadeEvent e) {
		if (e.getBlock().getType() == Material.FIRE) {
			Area area = Protection.getAreaManager().getArea(e.getBlock().getLocation());
			if (area != null && area.hasFlag(Flag.NO_FIRE_SPREAD)) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getLocation());
		if (area != null) {
			EntityType t = e.getEntityType();
			if (mobs.contains(t) && area.hasFlag(Flag.NO_MOBS) && REASONS.contains(e.getSpawnReason())) {
				e.setCancelled(true);
			}
			else if (animals.contains(t) && area.hasFlag(Flag.NO_ANIMALS) && REASONS.contains(e.getSpawnReason())) {
				e.setCancelled(true);
			}
			else if (area.hasFlag(Flag.STARTING_AREA) && e.getSpawnReason() == SpawnReason.NETHER_PORTAL) {
				e.setCancelled(true);
			}
		}
		if (e.getEntity().getType() == EntityType.WITHER && e.getSpawnReason() == SpawnReason.BUILD_WITHER && e.getLocation().getWorld().getEnvironment() != Environment.NETHER) {
			e.setCancelled(true);
			for (Entity entity : e.getEntity().getNearbyEntities(10, 10, 10)) {
				if (entity instanceof Player) {
					Player player = (Player) entity;
					player.sendMessage(CoreUtils.getErrorBaseColor() + "Witherin voi luoda ainoastaan Nether-maailmassa!");
				}
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
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (e.getFoodLevel() < player.getFoodLevel()) {
				Area area = Protection.getAreaManager().getArea(player.getLocation());
				if (area != null) {
					if (area.hasFlag(Flag.NO_HUNGER)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getPlayer().getLocation());
		if (area != null && area.hasFlag(Flag.STARTING_AREA)) {
			e.setCancelled(true);
			CoreUtils.startTutorial(e.getPlayer());
			e.getPlayer().stopSound(Sound.BLOCK_PORTAL_AMBIENT);
			e.getPlayer().stopSound(Sound.BLOCK_PORTAL_TRIGGER);
			e.getPlayer().stopSound(Sound.BLOCK_PORTAL_TRAVEL);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getPlayer().getLocation());
		if (area != null && area.hasFlag(Flag.STARTING_AREA)) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(CoreUtils.getErrorBaseColor() + "Et voi käyttää komentoja vielä!");
		}
	}
	
	private List<EntityType> mobs = Arrays.asList(EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, 
			EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GIANT, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.ILLUSIONER, 
			EntityType.MAGMA_CUBE, EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, 
			EntityType.SKELETON_HORSE, EntityType.SLIME, EntityType.SPIDER, EntityType.STRIDER, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, 
			EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN);
	
	private List<EntityType> animals = Arrays.asList(EntityType.BAT, EntityType.CHICKEN, EntityType.COD, EntityType.COW, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.FOX, EntityType.HORSE, 
			EntityType.IRON_GOLEM, EntityType.LLAMA, EntityType.MULE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PANDA, EntityType.PARROT, EntityType.PIG, EntityType.POLAR_BEAR, 
			EntityType.PUFFERFISH, EntityType.RABBIT, EntityType.SALMON, EntityType.SHEEP, EntityType.SNOWMAN, EntityType.SQUID, EntityType.TRADER_LLAMA, EntityType.TROPICAL_FISH, EntityType.TURTLE, 
			EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.WOLF);
	
	private List<String> throneMessages = Arrays.asList("§4Vartija: §cHei! Mitä ihmettä kuvittelet tekeväsi?!", "§4Vartija: §cAlas sieltä, heti!", 
			"§4Vartija: §cTämä on Kuninkaan valtaistuin, ei sinun!", "§4Vartija: §cSinulla ei ole mitään asiaa Hänen Majesteettinsa valtaistuimelle!");
}