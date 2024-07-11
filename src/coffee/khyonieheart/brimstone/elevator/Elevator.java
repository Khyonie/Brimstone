package coffee.khyonieheart.brimstone.elevator;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;

@FeatureIdentifier({ "elevators" })
public class Elevator implements Listener, Feature
{
	private static Set<Player> lockedPlayers = new HashSet<>();
	private static boolean isEnabled = true;

	@EventHandler
	public void onJump(PlayerMoveEvent event)
	{
		if (!(event.getTo().getY() > event.getFrom().getY()))
		{
			return;
		}

		Location loc = event.getPlayer().getLocation().subtract(0, 1, 0);
		World world = loc.getWorld();

		if (!world.getBlockAt(loc).getType().equals(Material.EMERALD_BLOCK))
		{
			return;
		}

		if (lockedPlayers.contains(event.getPlayer()))
		{
			return;
		}

		loc = loc.add(0, 1, 0);

		Block target;

		while (loc.getBlockY() < world.getMaxHeight())
		{
			loc = loc.add(0, 1, 0);
			target = world.getBlockAt(loc);

			if (target == null)
			{
				continue;
			}

			if (!target.getType().equals(Material.EMERALD_BLOCK))	
			{
				continue;
			}

			if (!event.getPlayer().teleport(loc.add(0, 1, 0)))
			{
				Message.send(event.getPlayer(), "§cTeleport location is outside of the world.");
				return;
			}

			// Lock player for 10 ticks
			lockedPlayers.add(event.getPlayer());
			Hyacinth.getScheduler().runTaskLater(Hyacinth.getInstance(), () -> {
				lockedPlayers.remove(event.getPlayer());
			}, 10);

			return;
		}
	}

	@EventHandler
	public void onCrouch(PlayerToggleSneakEvent event)
	{
		Location loc = event.getPlayer().getLocation()
			.subtract(0, 1, 0);

		World world = loc.getWorld();

		if (!world.getBlockAt(loc).getType().equals(Material.EMERALD_BLOCK))
		{
			return;
		}

		if (lockedPlayers.contains(event.getPlayer()))
		{
			return;
		}

		loc = loc.subtract(0, 1, 0);
		Block target;

		while (loc.getBlockY() > world.getMinHeight())
		{
			loc = loc.subtract(0, 1, 0);
			target = world.getBlockAt(loc);

			if (target == null)
			{
				continue;
			}

			if (!target.getType().equals(Material.EMERALD_BLOCK))
			{
				continue;
			}

			if (!event.getPlayer().teleport(loc.add(0, 1, 0)))
			{
				Message.send(event.getPlayer(), "§cTeleport location is outside of the world.");
				return;
			}

			// Lock player for 10 ticks
			lockedPlayers.add(event.getPlayer());
			Hyacinth.getScheduler().runTaskLater(Hyacinth.getInstance(), () -> {
				lockedPlayers.remove(event.getPlayer());
			}, 10);

			return;
		}
	}

	@Override
	public boolean isEnabled(String target) 
	{
		return isEnabled;
	}

	@Override
	public boolean kill(String target) 
	{
		if (!isEnabled)
		{
			return false;
		}

		isEnabled = false;

		return true;
	}

	@Override
	public boolean reenable(String target) 
	{
		if (isEnabled)
		{
			return false;
		}

		isEnabled = true;	

		return true;
	}
}
