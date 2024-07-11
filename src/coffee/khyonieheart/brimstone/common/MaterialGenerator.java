package coffee.khyonieheart.brimstone.common;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import coffee.khyonieheart.hyacinth.command.parser.SuggestionGenerator;

public class MaterialGenerator implements SuggestionGenerator
{
	private static List<String> materials = Arrays.asList(Material.values())
		.stream()
		.filter((material) -> {
			if (material.name().contains("LEGACY"))
			{
				return false;
			}
			return material.isBlock();
		})
		.map((material) -> {
			return material.name();
		})
		.toList();

	@Override
	public List<String> generateSuggestions() 
	{
		return materials;
	}
}
