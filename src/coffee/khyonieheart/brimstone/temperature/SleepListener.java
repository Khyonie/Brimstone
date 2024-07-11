package coffee.khyonieheart.brimstone.temperature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import coffee.khyonieheart.brimstone.events.WorldNewDayEvent;
import coffee.khyonieheart.brimstone.time.TimeManager;
import coffee.khyonieheart.brimstone.util.Actionbar;
import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;
import coffee.khyonieheart.hyacinth.print.Grammar;
import coffee.khyonieheart.hyacinth.util.Arrays;
import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import coffee.khyonieheart.hyacinth.util.marker.Nullable;

@FeatureIdentifier({ "coveredSleep" })
public class SleepListener implements Listener, Feature
{
	private static List<Player> sleepingPlayers = new ArrayList<>();
	private static BukkitTask sleepTimer;

	private static boolean isEnabled;

	private static Set<BlockFace> faces = Set.of(
		BlockFace.UP,
		BlockFace.NORTH,
		BlockFace.EAST,
		BlockFace.SOUTH,
		BlockFace.WEST,
		BlockFace.DOWN
	);

	@EventHandler
	public void onSleep(PlayerBedEnterEvent event)
	{
		Block bed = event.getBed();

		if (!event.getBedEnterResult().equals(BedEnterResult.OK))
		{
			return;
		}

		if (isEnabled)
		{
			Block[] nearby = nearbyBlocks(bed, 300, new ArrayList<>(), new ArrayList<>(), (block) -> { 
				if (block == null)
				{
					return false;
				}
				return !block.getType().isSolid();
				//return !block.getType().equals(Material.AIR);
			});

			if (nearby == null)
			{
				Actionbar.send(event.getPlayer(), "You may not rest here, this bed is too exposed.");
				if (!Actionbar.isLocked(event.getPlayer()))
				{
					Actionbar.lockPlayer(event.getPlayer(), 40L);
				}
				event.setUseBed(Event.Result.DENY);
				return;
			}
		}

		sleepingPlayers.add(event.getPlayer());
		World world = event.getPlayer().getWorld(); // Must be the overworld

		Actionbar.send(event.getPlayer(), sleepingPlayers.size() + "/" + (world.getPlayers().size() / 2) + Grammar.plural(sleepingPlayers.size(), " player", " players") + " sleeping");

		if (!Actionbar.isLocked(event.getPlayer()))
		{
			Actionbar.lockPlayer(event.getPlayer(), 40L);
		}

		if (sleepingPlayers.size() < (world.getPlayers().size() / 2.0) || world.getTime() < 12010)
		{
			return;
		}

		if (sleepTimer == null)
		{
			world.getPlayers().forEach(player -> {
				Actionbar.send(player, "Sleeping through this night.");

				if (!Actionbar.isLocked(player))
				{
					Actionbar.lockPlayer(player, 40L);
				}
			});
			sleepTimer = new BukkitRunnable()
			{
				@Override
				public void run() 
				{
					if (this.isCancelled())
					{
						return;
					}
					for (Player p : sleepingPlayers)
					{
						p.setStatistic(Statistic.TIME_SINCE_REST, 0);
					}
					sleepingPlayers.clear();
					TimeManager.setTime(0);
					sleepTimer = null;
					Bukkit.getPluginManager().callEvent(new WorldNewDayEvent(world));
				}
			}.runTaskLater(Hyacinth.getInstance(), 100L);
		}
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event)
	{
		if (sleepTimer == null)
		{
			return;
		}

		sleepingPlayers.remove(event.getPlayer());
		if (sleepingPlayers.size() < (event.getPlayer().getWorld().getPlayers().size() / 2))
		{
			event.getPlayer().getWorld().getPlayers().forEach(player -> Actionbar.send(player, "Not enough players are sleeping, the night will continue."));
			sleepTimer.cancel();
			sleepTimer = null;
			return;
		}
	}

	@EventHandler
	public void onBedLeave(PlayerBedLeaveEvent event)
	{
		if (sleepTimer == null)
		{
			return;
		}

		sleepingPlayers.remove(event.getPlayer());
		if (sleepingPlayers.size() < (event.getPlayer().getWorld().getPlayers().size() / 2))
		{
			event.getPlayer().getWorld().getPlayers().forEach(player -> Actionbar.send(player, "Not enough players are sleeping, the night will continue."));
			sleepTimer.cancel();
			sleepTimer = null;
			return;
		}
	}

	@SuppressWarnings("unused") // JDTLS Please shut up, "location" IS USED
	@Nullable
	private static Block[] nearbyBlocks(
		@NotNull Block seed, 
		int maxBlocks,
		@NotNull List<Block> open,
		@NotNull List<Block> closed,
		@NotNull Predicate<Block> filter
	) {
		if (open.size() == 0)
		{
			open.add(seed);
		}

		Location location;
		Block block;
		Block target;

		while (!open.isEmpty())
		{
			block = open.get(0);
			location = block.getLocation();
			open.remove(0);
			closed.add(block);

			for (BlockFace face : faces)
			{
				if (open.size() + closed.size() == maxBlocks)
				{
					return null;
				}

				target = block.getRelative(face);

				if (target.getType().equals(Material.CAMPFIRE))
				{
					open.addAll(closed);
					return Arrays.toArray(Block[].class, open);
				}

				if (closed.contains(target) || open.contains(target))
				{
					continue;
				}

				if (filter.test(target))
				{
					//world.spawnParticle(Particle.REDSTONE, target.getLocation().clone().add(0.5, 0.5, 0.5), 1, new DustOptions(Color.RED, 1.0f));
					open.add(target);
					continue;
				}
			}
		}

		open.addAll(closed);
		return Arrays.toArray(Block[].class, open);
	}

	@Override
	public boolean isEnabled(
		String target
	)
	{
		return switch (target)
		{
			case "coveredSleep" -> isEnabled;
			default -> false;
		};
	}

	@Override
	public boolean kill(
		String target
	)
	{
		return switch (target) 
		{
			case "coveredSleep" -> {
				if (isEnabled)
				{
					yield !(isEnabled = false);
				}

				yield false;
			}
			default -> false;
		};
	}

	@Override
	public boolean reenable(
		String target
	)
	{
		return switch (target) 
		{
			case "coveredSleep" -> {
				if (!isEnabled) 
				{
					yield isEnabled = true;
				}
				yield false;
			}
			default -> false;
		};
	}
}
