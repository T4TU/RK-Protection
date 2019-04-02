package me.t4tu.rkprotection.areas;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkprotection.Protection;

public class AreaListener implements Listener {
	
	private static final List<SpawnReason> REASONS = Arrays.asList(SpawnReason.CHUNK_GEN, SpawnReason.NATURAL, SpawnReason.ENDER_PEARL, SpawnReason.DISPENSE_EGG, SpawnReason.SLIME_SPLIT, 
			SpawnReason.LIGHTNING, SpawnReason.JOCKEY, SpawnReason.REINFORCEMENTS, SpawnReason.SPAWNER, SpawnReason.SILVERFISH_BLOCK, SpawnReason.MOUNT, SpawnReason.EGG);
	private static final List<Material> CONTAINERS = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.CRAFTING_TABLE, Material.FURNACE, Material.ANVIL, 
			Material.ENCHANTING_TABLE, Material.JUKEBOX, Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.BREWING_STAND);
	
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
							e.getPlayer().sendMessage("§c Saavut PvP-alueelle! Tällä alueella muut voivat tappaa sinut ja sinä voit tappaa muita. Et menetä tavaroita kuollessasi.");
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
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Area area = Protection.getAreaManager().getArea(e.getEntity().getLocation());
		if (area != null && area.hasFlag(Flag.PVP)) {
			e.setKeepInventory(true);
			e.setKeepLevel(true);
			e.setDroppedExp(0);
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
					if (area.hasFlag(Flag.ALLOW_DOORS) && (e.getClickedBlock().getType().toString().contains("DOOR") || 
							e.getClickedBlock().getType().toString().contains("TRAPDOOR") || 
							e.getClickedBlock().getType().toString().contains("FENCE_GATE"))) {
						return;
					}
					if (area.hasFlag(Flag.ALLOW_REDSTONE) && (e.getClickedBlock().getType().toString().contains("BUTTON") || 
							e.getClickedBlock().getType().toString().contains("PRESSURE_PLATE") || 
							e.getClickedBlock().getType().toString().contains("LEVER"))) {
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
					if (e.getClickedBlock().getType() == Material.FARMLAND) {
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
			if (mobs.contains(t) && area.hasFlag(Flag.NO_MOBS) && REASONS.contains(e.getSpawnReason())) {
				e.setCancelled(true);
			}
			else if (animals.contains(t) && area.hasFlag(Flag.NO_ANIMALS) && REASONS.contains(e.getSpawnReason())) {
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
	
	private List<EntityType> mobs = Arrays.asList(EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON,
			EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GIANT, EntityType.GUARDIAN, EntityType.HUSK, EntityType.ILLUSIONER, EntityType.MAGMA_CUBE,
			EntityType.PHANTOM, EntityType.PIG_ZOMBIE, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.SLIME, EntityType.SPIDER,
			EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER);
	
	private List<EntityType> animals = Arrays.asList(EntityType.BAT, EntityType.CHICKEN, EntityType.COD, EntityType.COW, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.HORSE,
			EntityType.IRON_GOLEM, EntityType.LLAMA, EntityType.MULE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PARROT, EntityType.PIG, EntityType.POLAR_BEAR, EntityType.PUFFERFISH,
			EntityType.RABBIT, EntityType.SALMON, EntityType.SHEEP, EntityType.SNOWMAN, EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.VILLAGER, EntityType.WOLF);
	
	private List<String> throneMessages = Arrays.asList("§4Vartija: §cHei! Mitä ihmettä kuvittelet tekeväsi?!", "§4Vartija: §cAlas sieltä, heti!", 
			"§4Vartija: §cTämä on Kuninkaan valtaistuin, ei sinun!", "§4Vartija: §cSinulla ei ole mitään asiaa Hänen Majesteettinsa valtaistuimelle!");
}