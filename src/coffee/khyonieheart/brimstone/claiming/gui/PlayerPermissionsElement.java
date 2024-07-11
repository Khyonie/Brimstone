package coffee.khyonieheart.brimstone.claiming.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import coffee.khyonieheart.brimstone.claiming.ClaimData;
import coffee.khyonieheart.brimstone.claiming.ClaimPermission;
import coffee.khyonieheart.brimstone.common.OfflinePlayerProfile;
import coffee.khyonieheart.brimstone.common.SkinData;
import coffee.khyonieheart.hibiscus.Element;
import coffee.khyonieheart.hibiscus.Hibiscus;

public class PlayerPermissionsElement implements Element
{
	private final String uuid;
	private final ClaimData claim;

	private static final String unknownPlayerHead = "http://textures.minecraft.net/texture/2705fd94a0c431927fb4e639b0fcfb49717e412285a02b439e0112da22b2e2ec";

	public PlayerPermissionsElement(String uuid, ClaimData claim)
	{
		this.uuid = uuid;
		this.claim = claim;
	}

	@Override
	public void onInteract(InventoryClickEvent event, Player player, int clickedSlot, InventoryAction action, InventoryView view, ItemStack itemOnCursor) 
	{
		Hibiscus.getOpenGui(player).addLayer(player, 0, uuid);
	}

	@Override
	public ItemStack toIcon() 
	{
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);

		OfflinePlayerProfile offlineProfile = SkinData.getProfile(uuid);
		PlayerProfile profile;

		if (offlineProfile == null)
		{
			profile = Bukkit.createPlayerProfile(UUID.fromString(uuid), "Unknown");

			try {
				profile.getTextures().setSkin(new URL(unknownPlayerHead));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
 			profile = offlineProfile.toProfile();
		}

		SkullMeta meta = (SkullMeta) item.getItemMeta();

		try {
			meta.setOwnerProfile(profile);
		} catch (IllegalArgumentException e) {}

		meta.setDisplayName("§eEdit " + profile.getName());

		List<String> lore = new ArrayList<>();
		for (ClaimPermission permission : ClaimPermission.values())
		{
			String s = String.format("%-25s", permission.displayName());
			s = s + "§r§7[ " + (claim.hasPermission(uuid, permission) ? "§a§l✔" : "§c§l✗") + " §r§7]";
			lore.add("§7§o" + s);
		}

		meta.setLore(lore);

		item.setItemMeta(meta);
		return item;
	}
}
