package coffee.khyonieheart.brimstone.claiming;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import coffee.khyonieheart.hyacinth.Hyacinth;

public class ClaimSpongeItem
{
	private static ItemStack item = new ItemStack(Material.WET_SPONGE);
	private static NamespacedKey key = new NamespacedKey(Hyacinth.getInstance(), "claim_sponge");

	static {
		ItemMeta meta = item.getItemMeta();

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(key, PersistentDataType.BYTE, (byte) 0x00);

		meta.setDisplayName("§eClaim Sponge");
		meta.setLore(List.of("§7§oPlace to claim a column of", "§7§o25x25 blocks, extending vertically."));

		item.setItemMeta(meta);

		ShapedRecipe recipe = new ShapedRecipe(key, item);

		recipe.shape("iii", "idi", "ggg");
		recipe.setIngredient('g', Material.GOLD_BLOCK);
		recipe.setIngredient('d', Material.DIAMOND);
		recipe.setIngredient('i', Material.IRON_INGOT);

		Bukkit.addRecipe(recipe);
	}

	public static ItemStack get()
	{
		return item;
	}

	public static NamespacedKey getKey()
	{
		return key;
	}
}
