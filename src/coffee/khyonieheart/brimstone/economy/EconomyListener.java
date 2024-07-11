package coffee.khyonieheart.brimstone.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import coffee.khyonieheart.brimstone.Brimstone;
import coffee.khyonieheart.brimstone.pylon.Pylon;
import coffee.khyonieheart.brimstone.pylon.PylonManager;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;
import coffee.khyonieheart.hyacinth.util.CastableMap;

@FeatureIdentifier({ "signShopBuy", "signShopSell" })
public class EconomyListener implements Listener, Feature
{
	private static boolean enableBuy = true, enableSell = true;

	public EconomyListener()
	{
		//KillswitchManager.register(Brimstone.class, this);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		CastableMap<String, Object> data = (CastableMap<String, Object>) HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance());
		if (!data.containsKey("money"))
		{
			HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance()).put("money", 250);
			Message.send(event.getPlayer(), "§aWelcome back! $250 has been added to your account.");
		}

		if (!data.containsKey("isHomed"))
		{
			HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance()).put("isHomed", false);
		}

		if (((CastableMap<String, Object>) HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance())).get("isHomed", Boolean.class))
		{
			boolean homed = false;

			for (Pylon p : PylonManager.getPylons())
			{
				if (p.getHomedPlayers().contains(event.getPlayer().getUniqueId().toString()))
				{
					homed = true;
					break;
				}
			}

			if (!homed)
			{
				HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance()).put("isHomed", false);
				Message.send(event.getPlayer(), "§eYour home pylon was removed. Your spawn has been reset.");
			}
		}

		if (Transactions.getPendingTransaction(event.getPlayer()).isSome())
		{
			int value = Transactions.getPendingTransaction(event.getPlayer()).unwrap(Integer.class);
			data.put("money", data.getInt("money") + value);

			Message.send(event.getPlayer(), "§bWelcome back! A pending transaction of $" + value + " has been applied to your account.");
		}
	}

	@EventHandler
	public void onSignCreate(BlockPlaceEvent event)
	{
		if (event.getBlock() == null)
		{
			return;
		}

		if (!event.getBlock().getType().name().contains("SIGN"))
		{
			return;
		}

		SignSide data = ((Sign) event.getBlock().getState()).getSide(Side.FRONT);

		if (data.getLine(3).equals("Shopkeeper"))
		{
			if (!event.getPlayer().hasPermission("brimstone.shopkeeper"))
			{
				return;
			}

			data.setLine(3, event.getPlayer().getDisplayName());
		}
	}

	@EventHandler
	public void onBlockInteract(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
		{
			return;
		}

		if (!event.getClickedBlock().getType().name().contains("SIGN"))
		{
			return;
		}

		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}

		// Check if sign is a shop sign
		Block block = event.getClickedBlock();
		SignSide signData = ((Sign) block.getState()).getSide(Side.FRONT);

		if (!signData.getLine(0).equals("[BUY]") && !signData.getLine(0).equals("[SELL]"))
		{
			return;
		}
		
		int cost;
		int amount;

		try {
			cost = Integer.parseInt(signData.getLine(1));
		} catch (NumberFormatException e) {
			Message.send(event.getPlayer(), "§cThis shop's price value is not valid. Expected a number, received \"" + signData.getLine(1) + "\"");
			return;
		}

		try {
			amount = Integer.parseInt(signData.getLine(2));
		} catch (NumberFormatException e) {
			Message.send(event.getPlayer(), "§cThis shop's item count value is not valid. Expected a number, received \"" + signData.getLine(1) + "\"");
			return;
		}

		if (signData.getLine(3).equals(event.getPlayer().getDisplayName()))
		{
			Message.send(event.getPlayer(), "§6You can't buy or sell items to yourself!");
			return;
		}

		// Load player data
		CastableMap<String, Object> playerData = (CastableMap<String, Object>) HyacinthDataManager.get(event.getPlayer(), Brimstone.getInstance());

		// Link to chest
		for (int i = 0; i < 10; i++)
		{
			block = block.getRelative(BlockFace.DOWN);

			if (block.getType().equals(Material.CHEST))
			{
				break;
			}
		}

		if (!block.getType().equals(Material.CHEST))
		{
			Message.send(event.getPlayer(), "§cThis shop is not linked to a chest!");
			return;
		}

		Chest chestData = (Chest) block.getState();
		Inventory inv = chestData.getBlockInventory();
		Material material = null;
		ItemMeta meta = null;

		for (int i = 0; i < inv.getSize(); i++)
		{
			if (inv.getItem(i) == null)
			{
				continue;
			}

			material = inv.getItem(i).getType();
			meta = inv.getItem(i).getItemMeta().clone();
		}

		if (material == null)
		{
			Message.send(event.getPlayer(), "§cThis shop has no items!");
			return;
		}

		switch (signData.getLine(0))
		{
			case "[BUY]" -> {
				if (!enableBuy)
				{
					Message.send(event.getPlayer(), "§eSorry, buying from sign shops has been temporarily disabled.");
					return;
				}

				if (playerData.getInt("money") < cost)
				{
					Message.send(event.getPlayer(), "§cYou don't have enough money!");
					return;
				}

				List<ItemStack> allItems = new ArrayList<>(inv.all(material).values());

				int inventoryAmount = 0;
				for (ItemStack i : allItems)
				{
					inventoryAmount += i.getAmount();
				}

				if (inventoryAmount < amount)
				{
					Message.send(event.getPlayer(), "§cThis shop does not have enough items to sell!");
					return;
				}

				// Perform the transaction
				inventoryAmount -= amount;
				playerData.put("money", playerData.getInt("money") - cost);

				ItemStack receivedItem = new ItemStack(material, amount);
				receivedItem.setItemMeta(meta);

				ItemStack newInventoryItem = new ItemStack(material, inventoryAmount);
				newInventoryItem.setItemMeta(meta);

				inv.clear();
				inv.addItem(newInventoryItem);

				Map<Integer, ItemStack> failedAddingItems = event.getPlayer().getInventory().addItem(receivedItem);
				failedAddingItems.forEach((i, item) -> event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), item));

				for (Player p : Bukkit.getOnlinePlayers())
				{
					if (p.getDisplayName().equals(signData.getLine(3)))
					{
						CastableMap<String, Object> targetData = (CastableMap<String, Object>) HyacinthDataManager.get(p, Brimstone.getInstance());
						targetData.put("money", targetData.getInt("money") + cost);

						Message.send(p, "§bA player has bought " + amount + " " + material.name().toLowerCase().replace("_", " ") + " from your sign shop. You have been credited $" + cost + ".");
						Message.send(event.getPlayer(), "§aTransaction complete!");

						return;
					}
				}

				Transactions.applyToPendingTransaction(signData.getLine(3), cost);

				Message.send(event.getPlayer(), "§aTransaction complete! Transaction will apply when player next logs in.");
			}
			case "[SELL]" -> {
				if (!enableSell)
				{
					Message.send(event.getPlayer(), "§eSorry, but selling to sign shops has been temporarily disabled.");
					return;
				}

				int maxCount = material.getMaxStackSize() * inv.getSize();
				int chestCount = 0;
				for (ItemStack i : inv.getContents())
				{
					if (i == null)
					{
						continue;
					}

					chestCount += i.getAmount();
				}

				if (chestCount + amount > maxCount)
				{
					Message.send(event.getPlayer(), "§cThis shop is full! Cannot sell right now.");
					return;
				}

				int invCount = 0;
				for (ItemStack i : event.getPlayer().getInventory().getContents())
				{
					if (i == null)
					{
						continue;
					}

					if (!i.getType().equals(material))
					{
						continue;
					}

					invCount += i.getAmount();
				}

				if (invCount < amount)
				{
					Message.send(event.getPlayer(), "§cYou don't have enough items to sell!");
					return;
				}

				// Perform transaction
				
				// Remove items from player inventory
				event.getPlayer().getInventory().remove(material);

				ItemStack playerItems = new ItemStack(material, invCount - amount);
				playerItems.setItemMeta(meta);
				event.getPlayer().getInventory().addItem(playerItems);

				// Add items to chest inventory
				inv.clear();
				ItemStack chestItems = new ItemStack(material, chestCount + amount);
				chestItems.setItemMeta(meta);
				inv.addItem(chestItems);

				playerData.put("money", playerData.getInt("money") + cost);

				for (Player p : Bukkit.getOnlinePlayers())
				{
					if (p.getDisplayName().equals(signData.getLine(3)))
					{
						CastableMap<String, Object> targetData = (CastableMap<String, Object>) HyacinthDataManager.get(p, Brimstone.getInstance());
						targetData.put("money", targetData.getInt("money") - cost);

						Message.send(event.getPlayer(), "§aTransaction complete!");
						Message.send(p, "§bA player has sold " + amount + " " + material.name().toLowerCase().replace("_", " ") + " to you, your account has been deducted $" + cost + ".");

						return;
					}
				}

				if (signData.getLine(3).equals("Shopkeeper"))
				{
					Message.send(event.getPlayer(), "§aTransaction complete!");
					return;
				}

				Transactions.applyToPendingTransaction(signData.getLine(3), -1 * cost);

				Message.send(event.getPlayer(), "§aTransaction complete! Transaction will apply when player next logs in.");
			}
			default -> {
				return;
			}
		}

	}

	@Override
	public boolean isEnabled(String target) 
	{
		return switch (target)
		{
			case "signShopBuy" -> enableBuy;
			case "signShopSell" -> enableSell;
			default -> false;
		};
	}

	@Override
	public boolean kill(String target) 
	{
		return switch (target)
		{
			case "signShopBuy" -> {
				if (enableBuy)
				{
					enableBuy = false;
					yield true;
				}

				yield false;
			}
			case "signShopSell" -> {
				if (enableSell)
				{
					enableSell = false;
					yield true;
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
			case "signShopBuy" -> {
				if (!enableBuy)
				{
					yield enableBuy = true;
				}

				yield false;
			}
			case "signShopSell" -> {
				if (!enableSell) 
				{
					yield enableSell = true;
				}

				yield false;
			}
			default -> false;
		};
	}
}
