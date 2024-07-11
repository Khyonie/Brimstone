package coffee.khyonieheart.brimstone.claiming.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import coffee.khyonieheart.brimstone.claiming.ClaimData;
import coffee.khyonieheart.brimstone.claiming.ClaimPermission;
import coffee.khyonieheart.crafthibiscus.HibiscusPagedGui;
import coffee.khyonieheart.hibiscus.Element;
import coffee.khyonieheart.hibiscus.GuiConfiguration;
import coffee.khyonieheart.hibiscus.Hibiscus;
import coffee.khyonieheart.hibiscus.element.ButtonElement;
import coffee.khyonieheart.hibiscus.element.ItemStackElement;

public class ClaimGui extends HibiscusPagedGui
{
	private ClaimGui(List<Element> elements) 
	{
		super("Edit Claim", elements);
	}

	public static ClaimGui create(Player player, ClaimData claim)
	{
		List<Element> data = new ArrayList<>();
		Set<String> processedUuids = new HashSet<>();
		data.add(new PlayerPermissionsElement(player.getUniqueId().toString(), claim));
		processedUuids.add(player.getUniqueId().toString());

		for (String s : claim.getAllPlayers())
		{
			if (processedUuids.contains(s))
			{
				continue;
			}

			data.add(new PlayerPermissionsElement(s, claim));
			processedUuids.add(s);
		}

		for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
		{
			if (processedUuids.contains(offlinePlayer.getUniqueId().toString()))
			{
				continue;
			}

			data.add(new PlayerPermissionsElement(offlinePlayer.getUniqueId().toString(), claim));
			processedUuids.add(offlinePlayer.getUniqueId().toString());
		}

		ClaimGui gui = new ClaimGui(data);

		for (String uuid : processedUuids)
		{
			GuiConfiguration config = new GuiConfiguration(uuid, genPlayerEditElement(uuid, claim));
			gui.addConfiguration(uuid, config);
		}

		return gui;
	}

	private static Map<Integer, Element> genPlayerEditElement(String uuid, ClaimData data)
	{
		Map<Integer, Element> layer = new HashMap<>();
		for (int i = 0; i < 54; i++)
		{
			layer.put(i, new ItemStackElement(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§r", 1));
		}

		layer.put(9, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.BREAK_BLOCKS));
		layer.put(10, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.PLACE_BLOCKS));
		layer.put(11, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.DAMAGE_PLAYERS));
		layer.put(12, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.DAMAGE_ENTITIES));

		layer.put(14, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.INTERACT_SIMPLE));
		layer.put(15, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.INTERACT_REDSTONE));
		layer.put(16, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.INTERACT_CONTAINERS));
		layer.put(17, new PlayerTogglePermissionElement(uuid, data, ClaimPermission.BREAK_CLAIM));

		ButtonElement saveButton = new ButtonElement(Material.WRITABLE_BOOK, "§bSave Permissions", 1);
		saveButton.setAction((event, player, slot, action, view, cursor) -> {
			Hibiscus.getOpenGui(player).removeLayer(player);
		});

		layer.put(31, saveButton);

		return layer;
	}
}
