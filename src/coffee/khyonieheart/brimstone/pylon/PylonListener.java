package coffee.khyonieheart.brimstone.pylon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;

import coffee.khyonieheart.brimstone.Brimstone;
import coffee.khyonieheart.crafthibiscus.HibiscusPagedGui;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hibiscus.Element;
import coffee.khyonieheart.hyacinth.Message;

public class PylonListener implements Listener
{
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent event)
	{
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}

		if (!event.getHand().equals(EquipmentSlot.HAND))
		{
			return;
		}

		if (event.getClickedBlock() == null)
		{
			return;
		}

		if (!event.getClickedBlock().getType().equals(Material.BELL))
		{
			return;
		}

		if (!Pylon.isPylonStructure(event.getClickedBlock()))
		{
			return;
		}

		for (Pylon p : PylonManager.getPylons())
		{
			if (p.isPylonBlock(event.getClickedBlock()))
			{
				event.setCancelled(true);

				List<Pylon> data = new ArrayList<>(PylonManager.getPylons());
				
				// Remove private pylons
				data.removeIf((pylon) -> {
					if (!pylon.isPublic())
					{
						if (!pylon.getAddedPlayers().contains(event.getPlayer().getUniqueId().toString()))
						{
							return true;
						}
					}

					return false;
				});

				List<Element> mappedData = data
					.stream()
					.map((pylon) -> { return (Element) new PylonElement(pylon, event.getPlayer()); })
					.toList();

				HibiscusPagedGui gui = new HibiscusPagedGui("Pylons (" + PylonManager.getPylons().size() + " available)", mappedData);
				gui.open(event.getPlayer());

				return;
			}
		}

		// Create new pylon
		event.setCancelled(true);
		Pylon p = new Pylon(event.getClickedBlock(), event.getPlayer());

		PylonManager.getPylons().add(p);
		Message.send(event.getPlayer(), "§aRegistered a new pylon here!");
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		if ((Boolean) HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance()).get("isHomed"))
		{
			for (Pylon p : PylonManager.getPylons())
			{
				if (p.getHomedPlayers().contains(event.getPlayer().getUniqueId().toString()))
				{
					event.setRespawnLocation(p.getLocation(null));
				}
			}
		}
	}

	//
	// Protection for pylons getting destroyed either accidentally or on purpose
	//

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		for (Pylon p : PylonManager.getPylons())
		{
			if (!p.isPylonBlock(event.getBlock()))
			{
				continue;
			}

			if (event.getPlayer().getUniqueId().toString().equals(p.getOwnerUuid()))
			{
				PylonManager.getPylons().remove(p);
				Message.send(event.getPlayer(), "§bYou have broken the pylon here.");
				return;
			}

			// Player does not have permission to break pylon block
			event.setCancelled(true);
			Message.send(event.getPlayer(), "§cYou do not have permission to break this pylon.");
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event)
	{
		event.blockList().removeIf((block) -> {
			for (Pylon p : PylonManager.getPylons())
			{
				if (p.isPylonBlock(block))
				{
					return true;
				}
			}

			return false;
		});
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		event.blockList().removeIf((block) -> {
			for (Pylon p : PylonManager.getPylons())
			{
				if (p.isPylonBlock(block))
				{
					return true;
				}
			}

			return false;
		});
	}
}
