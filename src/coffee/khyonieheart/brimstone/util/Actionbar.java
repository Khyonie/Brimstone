package coffee.khyonieheart.brimstone.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import coffee.khyonieheart.hyacinth.Hyacinth;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Actionbar
{
	private static Set<Player> lockedPlayers = new HashSet<>();

	public static void send(
		Player player, 
		String message
	) {
		if (!lockedPlayers.contains(player))
		{
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
		}
	}

	public static boolean isLocked(Player player)
	{
		return lockedPlayers.contains(player);
	}

	public static void lockPlayer(Player player, long time)
	{
		lockedPlayers.add(player);

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				lockedPlayers.remove(player);
			}
		}.runTaskLater(Hyacinth.getInstance(), time);
	}
}
