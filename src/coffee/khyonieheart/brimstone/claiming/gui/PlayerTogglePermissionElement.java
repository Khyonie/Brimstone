package coffee.khyonieheart.brimstone.claiming.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import coffee.khyonieheart.brimstone.claiming.ClaimData;
import coffee.khyonieheart.brimstone.claiming.ClaimPermission;
import coffee.khyonieheart.hibiscus.Element;
import coffee.khyonieheart.hibiscus.Hibiscus;

public class PlayerTogglePermissionElement implements Element
{
	private ClaimData claim;
	private String uuid;
	private ClaimPermission permission;

	public PlayerTogglePermissionElement(String uuid, ClaimData claim, ClaimPermission permission)
	{
		this.uuid = uuid;
		this.claim = claim;
		this.permission = permission;
	}

	@Override
	public void onInteract(
		InventoryClickEvent event,
		Player player,
		int slot,
		InventoryAction action,
		InventoryView view,
		ItemStack cursor
	) {
		if (!claim.isAdded(uuid))
		{
			claim.addPlayer(uuid, (byte) 0);
		}
		claim.togglePermission(uuid, permission);
		Hibiscus.getOpenGui(player).regenerate(player);
	}

	@Override
	public ItemStack toIcon() 
	{
		ItemStack item = new ItemStack((claim.getPermissions(uuid) & (1 << permission.getOffset())) > 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§eToggle " + permission.name().replace('_', ' ').toLowerCase());

		item.setItemMeta(meta);

		return item;
	}
}
