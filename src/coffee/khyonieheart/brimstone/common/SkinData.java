package coffee.khyonieheart.brimstone.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.profile.PlayerProfile;

import coffee.khyonieheart.hyacinth.util.JsonUtils;

public class SkinData implements Listener
{
	private static Map<String, OfflinePlayerProfile> cachedData = new HashMap<>();
	private static final String FILEPATH = "./BrimstoneData/texturecache.json";

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		cachedData.put(event.getPlayer().getUniqueId().toString(), new OfflinePlayerProfile(event.getPlayer()));
	}

	public static void load()
	{
		if (!new File(FILEPATH).exists())
		{
			JsonUtils.toFile(FILEPATH, cachedData);
		}

		try {
			cachedData = JsonUtils.fromJson(FILEPATH, new TypeToken<HashMap<String, OfflinePlayerProfile>>() {}.getType());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void save()
	{
		JsonUtils.toFile(FILEPATH, cachedData);
	}

	public static String getSkin(OfflinePlayer player)
	{
		if (player.isOnline())
		{
			return getProfile(player).getTextures().getSkin().toString();
		}

		return cachedData.get(player.getUniqueId().toString()).getSkinUrl();
	}
	
	public static OfflinePlayerProfile getProfile(String uuid)
	{
		return cachedData.get(uuid);
	}

	public static PlayerProfile getProfile(OfflinePlayer player)
	{
		try {
			return (PlayerProfile) player.getClass().getMethod("getPlayerProfile").invoke(player);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return Bukkit.createPlayerProfile(player.getUniqueId());
		}
	}
}
