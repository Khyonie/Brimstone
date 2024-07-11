package coffee.khyonieheart.brimstone;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import coffee.khyonieheart.brimstone.hydration.HydrationData;
import coffee.khyonieheart.brimstone.util.Actionbar;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.util.CastableMap;
import coffee.khyonieheart.hyacinth.util.marker.NotNull;

public class PlayerHUDHandler implements Listener
{
	private static Map<Player, BukkitTask> UPDATE_TIMERS = new HashMap<>();
	private static Map<Player, Integer> ARROW_SPINNING = new HashMap<>();
	//private static final String[] ARROWS = { "▝", "▗", "▖", "▘" };

	private static final double DEFAULT_HYDRATION_DRAIN_RATE = 0.000041666; // Roughly 20 minutes of movement to drain thirst
	private static final Map<Environment, Double> BIOME_MODIFIERS = Map.of(
		Environment.NORMAL, 1.0,
		Environment.NETHER, 6.0,
		Environment.THE_END, 4.5
	);


	private static final Map<Material, Double> CONSUMABLE_OBJECT_VALUES = ofMany(
		// Positive
		Material.POTION, 0.3,
		Material.APPLE, 0.05,
		Material.GOLDEN_APPLE, 0.03,
		Material.ENCHANTED_GOLDEN_APPLE, 1.0,
		Material.GLOW_BERRIES, 0.01,
		Material.MELON_SLICE, 0.1,
		Material.MUSHROOM_STEW, 0.07,
		Material.SWEET_BERRIES, 0.01,
		Material.MILK_BUCKET, 0.25,

		// Negative
		Material.BREAD, -0.02,
		Material.COOKED_BEEF, -0.05,
		Material.COOKED_CHICKEN, -0.05,
		Material.COOKED_PORKCHOP, -0.05,
		Material.TROPICAL_FISH, -0.07
	);

	public PlayerHUDHandler()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!HydrationData.isHydrationEnabled())
				{
					return;
				}
				for (Player p : Bukkit.getOnlinePlayers())
				{
					if (((double) HyacinthDataManager.get(p, "Brimstone").get("hydrationLevel")) == 0.0)
					{
						if (p.isDead())
						{
							continue;
						}
						((Damageable) p).damage(0.5);
					}
				}
			}
		}.runTaskTimer(Hyacinth.getInstance(), 40L, 1L);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		HyacinthDataManager.get(event.getPlayer(), "Brimstone").put("hydrationLevel", 0.45); // About 9:30 of hydrations
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		BukkitTask task = new BukkitRunnable()
		{
			@Override
			public void run() 
			{
				updateDisplay(event.getPlayer());
				
				ARROW_SPINNING.put(event.getPlayer(), ARROW_SPINNING.get(event.getPlayer()) + 1);
				if (ARROW_SPINNING.get(event.getPlayer()) == 4)
				{
					ARROW_SPINNING.put(event.getPlayer(), 0);
				}
			}
		}.runTaskTimer(Hyacinth.getInstance(), 2L, 1L);
		ARROW_SPINNING.put(event.getPlayer(), 0);
		UPDATE_TIMERS.put(event.getPlayer(), task);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		UPDATE_TIMERS.get(event.getPlayer()).cancel();
		UPDATE_TIMERS.remove(event.getPlayer());
		ARROW_SPINNING.remove(event.getPlayer());
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!HydrationData.isHydrationEnabled())
		{
			return;
		}

		double hydration = (double) HyacinthDataManager.get(event.getPlayer(), "Brimstone").get("hydrationLevel");
		HyacinthDataManager.get(event.getPlayer(), "Brimstone").put("hydrationLevel", Math.max(0.0, hydration - (DEFAULT_HYDRATION_DRAIN_RATE * BIOME_MODIFIERS.get(event.getPlayer().getLocation().getWorld().getEnvironment()))));
	}

	@EventHandler
	public void onPlayerConsume(PlayerItemConsumeEvent event)
	{
		if (!CONSUMABLE_OBJECT_VALUES.containsKey(event.getItem().getType()))
		{
			return;
		}

		if (!HydrationData.isHydrationEnabled())
		{
			return;
		}

		double hydration = (double) HyacinthDataManager.get(event.getPlayer(), "Brimstone").get("hydrationLevel");
		hydration = Math.min(1.5, hydration + CONSUMABLE_OBJECT_VALUES.get(event.getItem().getType())); // Upper bound
		hydration = Math.max(0.0, hydration); // Lower bound
		
		HyacinthDataManager.get(event.getPlayer(), "Brimstone").put("hydrationLevel", hydration);
	}

	private static void updateDisplay(
		@NotNull Player target
	) {
		// Handle temperature
		//double temp = TemperatureHandler.getTemperature(target.getLocation());
		double hydration = (double) HyacinthDataManager.get(target, "Brimstone").get("hydrationLevel");

		// Initialize hydration meter
		char[] barData = new char[10];
		for (int i = 0; i < barData.length; i++)
		{
			barData[i] = '░';
		}

		char barColor = 'b';
		int fullBars = (int) Math.round(Math.floor(hydration / 0.1));
		if (fullBars > 10)
		{
			barColor = 'e';
		}
		if (fullBars < 5)
		{
			barColor = '6';
		}
		if (fullBars < 2)
		{
			barColor = 'c';
		}
		if (fullBars > 10)
			fullBars = 10;

		for (int i = 0; i < fullBars; i++)
		{
			barData[i] = '█';
		}
		
		String bar = "§bMoney: $" + ((CastableMap<String, Object>) HyacinthDataManager.get(target, Brimstone.getInstance())).getInt("money") + (HydrationData.isHydrationEnabled() ? " Water: [ §" + barColor + new String(barData) + "§b ]" : "");
		Actionbar.send(target, bar);

		//Actionbar.send(target, "§6§lLocal temperature: " + (temp * 100) + "° C" + (TemperatureHandler.generatingChunks().size() == 0 ? "" : " (" + ARROWS[ARROW_SPINNING.get(target)] + " Generating " + TemperatureHandler.generatingChunks().size() + ")"));
	}

	@SuppressWarnings("unused")
	private static String createBar(double percent, char filled, char empty)
	{
		StringBuilder builder = new StringBuilder(10);

		int percentval = (int) Math.round(percent);
		for (int i = 0; i < percentval; i++)
		{
			builder.append(filled);
		}

		for (int i = 0; i < (10 - percentval); i++)
		{
			builder.append(empty);
		}

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private static <K, V> Map<K, V> ofMany(Object... input)
	{
		Map<K, V> data = new HashMap<>();

		for (int i = 0; i < input.length; i++)
		{
			data.put((K) input[i], (V) input[++i]);
		}

		return data;
	}
}
