package coffee.khyonieheart.brimstone.pylon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import coffee.khyonieheart.brimstone.Brimstone;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hibiscus.Element;
import coffee.khyonieheart.hibiscus.Hibiscus;
import coffee.khyonieheart.hyacinth.Message;

public class PylonElement implements Element
{
	private Pylon pylon;
	private Player player;

	public PylonElement(Pylon pylon, Player player)
	{
		this.pylon = pylon;
		this.player = player;
	}

	@Override
	public void onInteract(InventoryClickEvent event, Player player, int slot, InventoryAction action, InventoryView view, ItemStack arg5) 
	{
		switch (action)
		{
			case PICKUP_ALL -> { // Teleport
				view.close();
				player.teleport(pylon.getLocation(player.getLocation().getDirection()));
				player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
			}
			case PICKUP_HALF -> { // Set home
				if (!pylon.getOwnerUuid().equals(player.getUniqueId().toString()))
				{
					if (!pylon.getAddedPlayers().contains(player.getUniqueId().toString()))
					{
						Message.send(player, "§cYou cannot set your home here.");
						return;
					}
				}

				if (pylon.getHomedPlayers().contains(player.getUniqueId().toString()))
				{
					Message.send(player, "§bYou already have your home here.");
					return;
				}

				player.setBedSpawnLocation(pylon.getLocation(player.getLocation().getDirection()), true);
				HyacinthDataManager.get(player, Brimstone.getInstance()).put("isHomed", true);
				pylon.getHomedPlayers().add(player.getUniqueId().toString());

				int index = 9;
				for (Pylon p : PylonManager.getPylons())
				{
					if (p.equals(pylon))
					{
						index++;
						continue;
					}

					if (p.getHomedPlayers().remove(player.getUniqueId().toString()))
					{
						Hibiscus.getOpenGui(player).regenerate(player, index);
					}

					index++;
				}

				Message.send(player, "§bYou have set your home here.");

				Hibiscus.getOpenGui(player).regenerate(player, slot);
			}
			default -> {
				return;
			}
		}
	}

	@Override
	public ItemStack toIcon() 
	{
		ItemStack item = new ItemStack(pylon.getIcon());

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§r" + pylon.getName());
		List<String> lore = new ArrayList<>();

		lore.add("§7§oBuilt by " + pylon.getOwnerName());

		Vector lookDirection = player.getLocation().getDirection();

		if (pylon.getAddedPlayers().contains(player.getUniqueId().toString()))
		{
			Location location = pylon.getLocation(lookDirection);

			String worldType = switch (location.getWorld().getEnvironment()) {
				case CUSTOM -> "Unknown";
				case NETHER -> "Nether";
				case NORMAL -> "Overworld";
				case THE_END -> "End";
			};

			lore.add("§7§o" + worldType + " at (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");

			lore.add("");

			if (pylon.getHomedPlayers().contains(player.getUniqueId().toString()))
			{
				lore.add("§7§oYou will respawn here.");

				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				meta.setLore(lore);

				item.setItemMeta(meta);
				item.addUnsafeEnchantment(Enchantment.RIPTIDE, 0);

				return item;
			}

			lore.add("§7§oRight-click to set spawn here.");
		}

		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}
}
