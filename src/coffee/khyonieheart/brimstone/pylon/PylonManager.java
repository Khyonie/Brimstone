package coffee.khyonieheart.brimstone.pylon;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.util.JsonUtils;

public class PylonManager
{
	private static List<Pylon> loadedPylons = new ArrayList<>();

	public static List<Pylon> getPylons()
	{
		return loadedPylons;
	}
	
	@SuppressWarnings("unchecked")
	public static void load()
	{
		File target = new File("./BrimstoneData/pylons.json");

		if (!target.exists())
		{
			JsonUtils.toFile(target.getAbsolutePath(), loadedPylons);
		}

		try {
			loadedPylons = (List<Pylon>) JsonUtils.fromJson(target.getAbsolutePath(), new TypeToken<ArrayList<Pylon>>() {}.getType());

			loadedPylons.forEach(p -> { 
				p.loadPylonStructure(); 
				p.update();
			});
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}

	public static List<Pylon> getPylonsMatchingName(Player player, String name)
	{
		List<Pylon> foundPylons = new ArrayList<>();

		for (Pylon p : loadedPylons)
		{
			if (!p.isPublic() && !p.getAddedPlayers().contains(player.getUniqueId().toString()))
			{
				continue;
			}

			if (p.getName().equals(name))
			{
				foundPylons.add(p);
			}
		}

		return foundPylons;
	}

	public static Pylon getPylonByName(Player player, String name)
	{
		List<Pylon> foundPylons = getPylonsMatchingName(player, name);

		if (foundPylons.isEmpty())
		{
			return null;
		}

		if (foundPylons.size() == 1)
		{
			return foundPylons.get(0);
		}

		// Prefer private pylon
		for (Pylon p : foundPylons)
		{
			if (!p.isPublic())
			{
				return p;
			}
		}

		return null;
	}

	public static void save()
	{
		JsonUtils.toFile("./BrimstoneData/pylons.json", loadedPylons);
	}
}
