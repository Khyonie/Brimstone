package coffee.khyonieheart.brimstone.anvil;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import coffee.khyonieheart.hibiscus.Hibiscus;
import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;

@FeatureIdentifier({ "fancyAnvils", "inventoryEnchants"})
public class AnvilListener implements Listener, Feature
{
	private static boolean isEnabled = true;
	private static boolean inventoryEnchants = false;

	@EventHandler
	public void onAnvilUse(PrepareAnvilEvent event)
	{
		if (!isEnabled)
		{
			return;
		}

		// Remove "Too expensive!" message by capping cost at 20
		event.getInventory().setMaximumRepairCost(Integer.MAX_VALUE);
		event.getInventory().setRepairCost(Math.min(20, event.getInventory().getRepairCost()));

		String newName;

		if ((newName = event.getInventory().getRenameText()) != null)
		{
			if (newName.equals(""))
			{
				return;
			}
			// Rename is attempted, apply color codes and remove magic characters
			newName = newName.replace('&', '§').replace("§k", "");

			ItemStack result = event.getResult();

			if (result == null)
			{
				return;
			}

			ItemMeta meta = result.getItemMeta();
			meta.setDisplayName(newName);
			result.setItemMeta(meta);

			if (event.getInventory().getItem(1) == null)
			{
				// Set cost to 0
				event.getInventory().setRepairCost(0);
				return;
			}

			// Otherwise reduce cost by 1
			event.getInventory().setRepairCost(Math.min(20, (event.getInventory().getRepairCost()) - 1));
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (!inventoryEnchants)
		{
			return;
		}

		if (Hibiscus.isInGui((Player) event.getWhoClicked()))
		{
			return;
		}

		if (event.getCursor() == null)
		{
			return;
		}

		if (!event.getCursor().getType().equals(Material.ENCHANTED_BOOK))
		{
			return;
		}

		if (!event.getAction().equals(InventoryAction.PLACE_ALL) && !event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR))
		{
			return;
		}

		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) event.getCursor().getItemMeta();
		ItemStack target = event.getCurrentItem();

		if (target == null)
		{
			return;
		}

		if (!supportsAtLeastOne(target, meta.getStoredEnchants().keySet()))
		{
			return;
		}
		
		meta.getStoredEnchants().forEach((enchantment, level) -> {
			if (target.containsEnchantment(enchantment))
			{
				if (target.getEnchantmentLevel(enchantment) >= level)
				{
					return;
				}
			}

			if (!enchantment.canEnchantItem(target))
			{
				return;
			}

			for (Enchantment targetEnch : target.getEnchantments().keySet())
			{
				if (targetEnch.conflictsWith(enchantment))
				{
					// TODO Allow certain conflicts
					return;
				}
			}

			target.addEnchantment(enchantment, level);
		});

		event.setCancelled(true);

		event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
		((Player) event.getWhoClicked()).updateInventory();
	}

	private static boolean supportsAtLeastOne(ItemStack item, Set<Enchantment> enchantments)
	{
		for (Enchantment e : enchantments)
		{
			if (e.canEnchantItem(item))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isEnabled(String target) 
	{
		return switch (target)
		{
			case "fancyAnvils" -> isEnabled;
			case "inventoryEnchants" -> inventoryEnchants;
			default -> false;
		};
	}

	@Override
	public boolean kill(String target)
	{
		return switch (target) 
		{
			case ("fancyAnvils") -> {
				if (isEnabled)
				{
					yield !(isEnabled = false);
				}

				yield false;
			}
			case "inventoryEnchants" -> {
				if (inventoryEnchants)
				{
					yield !(isEnabled = false);
				}

				yield false;
			}
			default -> false;
		};
	}

	@Override
	public boolean reenable(String target) 
	{
		return switch (target) 
		{
			case "fancyAnvils" -> {
				if (!isEnabled)
				{
					yield isEnabled = true;
				}

				yield false;
			}
			case "inventoryEnchants" -> {
				if (inventoryEnchants)
				{
					yield inventoryEnchants = true;
				}

				yield false;
			}
			default -> false;
		};
	}
}
