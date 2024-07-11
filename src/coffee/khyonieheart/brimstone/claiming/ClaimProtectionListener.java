package coffee.khyonieheart.brimstone.claiming;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import coffee.khyonieheart.hyacinth.Message;

public class ClaimProtectionListener implements Listener
{
	private static final Set<Material> CONTAINER_MATERIALS = Set.of(Material.CHEST, Material.TRAPPED_CHEST, Material.HOPPER, Material.SHULKER_BOX, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.BARREL, Material.DISPENSER, Material.DROPPER, Material.BREWING_STAND, Material.JUKEBOX, Material.LECTERN, Material.ITEM_FRAME); 
	private static final Set<Material> INTERACTABLE_MATERIALS = Set.of(Material.OAK_DOOR, Material.OAK_TRAPDOOR, Material.OAK_FENCE_GATE, Material.SPRUCE_DOOR, Material.SPRUCE_TRAPDOOR, Material.SPRUCE_FENCE_GATE, Material.BIRCH_DOOR, Material.BIRCH_TRAPDOOR, Material.BIRCH_FENCE_GATE, Material.JUNGLE_DOOR, Material.JUNGLE_TRAPDOOR, Material.JUNGLE_FENCE_GATE, Material.ACACIA_DOOR, Material.ACACIA_TRAPDOOR, Material.ACACIA_FENCE_GATE, Material.DARK_OAK_DOOR, Material.DARK_OAK_TRAPDOOR, Material.DARK_OAK_FENCE_GATE, Material.MANGROVE_DOOR, Material.MANGROVE_TRAPDOOR, Material.MANGROVE_FENCE_GATE, Material.WARPED_DOOR, Material.WARPED_TRAPDOOR, Material.WARPED_FENCE_GATE, Material.CRIMSON_DOOR, Material.CRIMSON_TRAPDOOR, Material.CRIMSON_FENCE_GATE, Material.ANVIL, Material.BEACON, Material.BEEHIVE, Material.BEE_NEST, Material.CAKE, Material.PUMPKIN, Material.COMPOSTER, Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE, Material.GRASS_BLOCK, Material.GRINDSTONE, Material.ITEM_FRAME, Material.LODESTONE, Material.LOOM, Material.NOTE_BLOCK, Material.STONECUTTER, Material.SMITHING_TABLE);
	private static final Set<Material> REDSTONE_MATERIALS = Set.of(Material.REDSTONE_WIRE, Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON, Material.MANGROVE_BUTTON, Material.WARPED_BUTTON, Material.CRIMSON_BUTTON, Material.STONE_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON, Material.LEVER, Material.COMPARATOR, Material.REPEATER, Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.MANGROVE_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE);

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		for (ClaimData data : ClaimManager.getClaims())
		{
			if (!data.contains(event.getBlock()))
			{
				continue;
			}

			if (!data.hasPermission(event.getPlayer(), ClaimPermission.PLACE_BLOCKS) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
			{
				Message.send(event.getPlayer(), "§cYou do not have permission to build in this area.");
				event.setCancelled(true);
				return;	
			}
		}
		if (!event.getBlock().getType().equals(Material.WET_SPONGE))
		{
			return;
		}

		if (event.getItemInHand().getItemMeta().getPersistentDataContainer().has(ClaimSpongeItem.getKey(), PersistentDataType.BYTE))
		{
			event.getPlayer().playSound(event.getBlock().getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);

			ClaimData data = new ClaimData(event.getBlock().getLocation(), event.getPlayer());

			if (data.overlapsNotOwned())
			{
				Message.send(event.getPlayer(), "§cYou cannot overlap a claim that you do not own.");
				event.setCancelled(true);
				return;
			}

			ClaimManager.getClaims().add(data);
			Message.send(event.getPlayer(), "§7You have claimed the surrounding 25x25 area.");
		}
	}

	@EventHandler
	public void onBucketFill(
		PlayerBucketFillEvent event
	) {
		if (event.getPlayer().hasPermission("brimstone.claims.bypass"))
		{
			return;
		}

		for (ClaimData data : ClaimManager.getClaims())
		{
			if (!data.contains(event.getBlock()))
			{
				continue;
			}

			if (!data.hasPermission(event.getPlayer(), ClaimPermission.BREAK_BLOCKS) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
			{
				Message.send(event.getPlayer(), "§cYou do not have permission to remove liquids in this area.");
				event.setCancelled(true);
				return;	
			}
		}
	}

	@EventHandler
	public void onBucketEmpty(
		PlayerBucketEmptyEvent event
	) {
		if (event.getPlayer().hasPermission("brimstone.claims.bypass"))
		{
			return;
		}

		for (ClaimData data : ClaimManager.getClaims())
		{
			if (!data.contains(event.getBlock()))
			{
				continue;
			}

			if (!data.hasPermission(event.getPlayer(), ClaimPermission.PLACE_BLOCKS) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
			{
				Message.send(event.getPlayer(), "§cYou do not have permission to place liquids in this area.");
				event.setCancelled(true);
				return;	
			}
		}
	}
	
	@EventHandler
	public void onLiquidFlow(
		BlockFromToEvent event
	) {
		ClaimData toClaim;
		if ((toClaim = ClaimManager.getClaim(event.getToBlock())) == null)
		{
			return;
		}

		ClaimData fromClaim;
		if ((fromClaim = ClaimManager.getClaim(event.getBlock())) == null)
		{
			if (!toClaim.allowsExternalLiquidFlow())
			{
				event.setCancelled(true);
				return;
			}

			return;
		}

		if (toClaim.getOwner().equals(fromClaim.getOwner()))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		ClaimData brokenClaim = null;

		for (ClaimData data : ClaimManager.getClaims())
		{
			if (!data.contains(event.getBlock()))
			{
				continue;
			}

			if (!data.hasPermission(event.getPlayer(), ClaimPermission.BREAK_BLOCKS) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
			{
				Message.send(event.getPlayer(), "§cYou do not have permission to break blocks in this area.");
				event.setCancelled(true);
				return;
			}

			if (!event.getBlock().getType().equals(Material.WET_SPONGE))
			{
				return;
			}

			if (!data.isClaimBlock(event.getBlock()))
			{
				continue;
			}

			if (!data.hasPermission(event.getPlayer(), ClaimPermission.BREAK_CLAIM) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
			{
				Message.send(event.getPlayer(), "§cYou do not have permission to remove the claim block here.");
				event.setCancelled(true);
				return;
			}

			brokenClaim = data;
			break;
		}

		if (brokenClaim == null)
		{
			return;
		}

		ClaimManager.getClaims().remove(brokenClaim);
		Message.send(event.getPlayer(), "§7You have removed the claim here.");
		event.setDropItems(false);
		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ClaimSpongeItem.get());
	}

	@EventHandler
	public void onBlockInteract(PlayerInteractEvent event)
	{
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}

		if (event.getClickedBlock() == null)
		{
			return;
		}

		if (event.getPlayer().hasPermission("brimstone.claims.bypass"))
		{
			return;
		}

		if (CONTAINER_MATERIALS.contains(event.getClickedBlock().getType()))
		{
			for (ClaimData data : ClaimManager.getClaims())
			{
				if (!data.contains(event.getClickedBlock()))
				{
					continue;
				}

				if (!data.hasPermission(event.getPlayer(), ClaimPermission.INTERACT_CONTAINERS) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
				{
					Message.send(event.getPlayer(), "§cYou do not have permission to open containers here.");
					event.setCancelled(true);
					return;
				}
			}

			return;
		}

		if (INTERACTABLE_MATERIALS.contains(event.getClickedBlock().getType()))
		{
			for (ClaimData data : ClaimManager.getClaims())
			{
				if (!data.contains(event.getClickedBlock()))
				{
					continue;
				}

				if (!data.hasPermission(event.getPlayer(), ClaimPermission.INTERACT_SIMPLE) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
				{
					Message.send(event.getPlayer(), "§cYou do not have permission to interact with simple blocks here.");
					event.setCancelled(true);
					return;
				}
			}

			return;
		}

		if (REDSTONE_MATERIALS.contains(event.getClickedBlock().getType()))
		{
			for (ClaimData data : ClaimManager.getClaims())
			{
				if (!data.contains(event.getClickedBlock()))
				{
					continue;
				}

				if (!data.hasPermission(event.getPlayer(), ClaimPermission.INTERACT_REDSTONE) && !event.getPlayer().hasPermission("brimstone.claims.bypass"))
				{
					Message.send(event.getPlayer(), "§cYou do not have permission to interact with redstone objects here.");
					event.setCancelled(true);
					return;
				}
			}

			return;
		}
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!(event.getDamager() instanceof Player))
		{
			return;
		}

		Block block = event.getEntity().getLocation().getBlock();

		for (ClaimData data : ClaimManager.getClaims())
		{
			if (!data.contains(block))
			{
				continue;
			}

			if (event.getEntity() instanceof Player)
			{
				if (!data.hasPermission((Player) event.getDamager(), ClaimPermission.DAMAGE_PLAYERS))
				{
					Message.send(event.getDamager(), "§cYou do not have permission to harm players here.");
					event.setCancelled(true);
					return;
				}
			}

			if (!data.hasPermission((Player) event.getDamager(), ClaimPermission.DAMAGE_ENTITIES))
			{
				Message.send(event.getDamager(), "§cYou do not have permission to harm creatures here.");
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event)
	{
		for (ClaimData data : ClaimManager.getClaims())
		{
			event.blockList().remove(data.getClaimBlock());
			if (data.allowsExplosions())
			{
				continue;
			}
			data.removeExplosionTargets(event.blockList());
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		for (ClaimData data : ClaimManager.getClaims())
		{
			event.blockList().remove(data.getClaimBlock());
			if (data.allowsExplosions())
			{
				continue;
			}
			data.removeExplosionTargets(event.blockList());
		}
	}
}
