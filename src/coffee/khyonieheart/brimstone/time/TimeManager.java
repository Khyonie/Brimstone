package coffee.khyonieheart.brimstone.time;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import coffee.khyonieheart.brimstone.events.WorldNewDayEvent;
import coffee.khyonieheart.brimstone.events.WorldNightStartEvent;
import coffee.khyonieheart.hyacinth.Hyacinth;

public class TimeManager
{
	private static long time;
	private static BukkitTask timeTask;

	private static boolean isDay = true;

	public static boolean startTime(World world, long period)
	{
		if (timeTask != null)
		{
			return false;
		}

		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		time = world.getTime();

		timeTask = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (this.isCancelled())
				{
					return;
				}

				if (time >= 13000 && isDay)
				{
					Bukkit.getPluginManager().callEvent(new WorldNightStartEvent(world));
					isDay = false;
				}

				if (world.getTime() != time)
				{
					world.setTime(time);
					return;
				}

				world.setTime(++time);

				if (time == 24000)
				{
					time = 0;
					Bukkit.getPluginManager().callEvent(new WorldNewDayEvent(world));
					isDay = true;
				}

				if (time < 13000 && !isDay)
				{
					isDay = true;
				}
			}
		}.runTaskTimer(Hyacinth.getInstance(), 1L, period);

		return true;
	}

	public static boolean stopIncreasedTime(World world)
	{
		if (timeTask == null)
		{
			return false;
		}

		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		timeTask.cancel();
		timeTask = null;

		return true;
	}

	public static void setTime(long newTime)
	{
		time = newTime;
	}
}
