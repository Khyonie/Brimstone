package coffee.khyonieheart.brimstone.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ItemStack;

import coffee.khyonieheart.brimstone.economy.EnemyDropListener;

public class DailyEventHandler implements Listener
{
	private static Map<Class<? extends Event>, RandomEvent> eventHandlers = new HashMap<>(); 
	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	private static Map<Class<? extends Event>, List<RandomEvent>> possibleDayEvents = new HashMap<>();
	private static Map<Class<? extends Event>, List<RandomEvent>> possibleNightEvents = new HashMap<>();

	private static final Set<EntityType> NIGHTTIME_SPAWNS = Set.of(
		EntityType.ZOMBIE,
		EntityType.CREEPER,
		EntityType.SPIDER,
		EntityType.SKELETON,
		EntityType.ENDERMAN,
		EntityType.WITCH,
		EntityType.DROWNED,
		EntityType.ZOMBIE_VILLAGER,
		EntityType.STRAY,
		EntityType.HUSK
	);

	private static final Set<Material> CROPS = Set.of(
		Material.WHEAT,
		Material.BEETROOTS,
		Material.CARROTS,
		Material.POTATOES,
		Material.COCOA,
		Material.MELON_STEM,
		Material.NETHER_WART,
		Material.PUMPKIN_STEM
	);

	public static Map<Class<? extends Event>, RandomEvent> getActiveEvents()
	{
		return eventHandlers;
	}

	static {
		// Day events

		// Night events
		registerNightEvent(EntitySpawnEvent.class, new RandomEvent("§c[ The apocolypse ]", "§7- All nighttime spawns are replaced with zombies.", (event) -> {
			EntitySpawnEvent e = (EntitySpawnEvent) event;

			if (!NIGHTTIME_SPAWNS.contains(e.getEntityType()))
			{
				return;
			}

			if (e.getEntityType().equals(EntityType.ZOMBIE) || e.getEntityType().equals(EntityType.ZOMBIE_VILLAGER))
			{
				return;
			}

			if (!e.getLocation().getWorld().getEnvironment().equals(Environment.NORMAL))
			{
				return;
			}

			if (e.getLocation().getBlockY() < e.getLocation().getWorld().getSeaLevel())
			{
				return;
			}

			if (e.getLocation().getWorld().getBlockAt(e.getLocation()).getLightFromSky() < 13)
			{
				return;
			}

			e.setCancelled(true);
			e.getEntity().getWorld().spawnEntity(e.getLocation(), random.nextInt(20) == 0 ? EntityType.ZOMBIE : EntityType.ZOMBIE_VILLAGER);
		}, null));

		registerNightEvent(EntitySpawnEvent.class, new RandomEvent("§c[ Silent night ]", "§7- All nighttime spawns are disabled.", (event) -> {
			EntitySpawnEvent e = (EntitySpawnEvent) event;
			if (!(NIGHTTIME_SPAWNS.contains(e.getEntityType())))
			{
				return;
			}

			if (!e.getLocation().getWorld().getEnvironment().equals(Environment.NORMAL))
			{
				return;
			}

			if (e.getLocation().getBlockY() < e.getLocation().getWorld().getSeaLevel())
			{
				return;
			}

			if (e.getLocation().getWorld().getBlockAt(((EntitySpawnEvent) event).getLocation()).getLightFromSky() >= 13)
			{
				return;
			}

			e.setCancelled(true);
		}, null));

		registerDayEvent(PlayerMoveEvent.class, new RandomEvent("§a[ Slime rain ]", "§7- Slime falls from the sky!", (event) -> {
			// Approx. 1 proc every 30 seconds if clear, 20 if raining
			if (random.nextInt(((PlayerMoveEvent) event).getPlayer().getWorld().isClearWeather() ? 600 : 400) != 0)
			{
				return;
			}

			// Pick an offset
			int xOffset = -5 + random.nextInt(10);
			int zOffset = -5 + random.nextInt(10);
			World world = ((PlayerMoveEvent) event).getPlayer().getWorld();

			int failsafe = 0;
			Location location;
			while (true)
			{
				if (++failsafe > 10)
				{
					// Too many failed invalid points, skip for now
					return;
				}

				// Check offsets
				location = ((PlayerMoveEvent) event).getPlayer().getLocation().add(xOffset, 0, zOffset);
				if (world.getHighestBlockAt(location).getLightFromSky() >= 13)
				{
					break;
				}
			}

			// Put spawnpoint up high
			location = location.add(0, 50, 0);

			// 90% chance to spawn slime, 10% chance to drop slimeball stack of 1-3
			switch (random.nextInt(10))
			{
				case 0 -> {
					// Slimeball, stack 1-3
					ItemStack item = new ItemStack(Material.SLIME_BALL, 1 + random.nextInt(3));
					((PlayerMoveEvent) event).getPlayer().getWorld().dropItem(location, item);
				}
				default -> {
					// Slime entity
					((PlayerMoveEvent) event).getPlayer().getWorld().spawnEntity(location, EntityType.SLIME);
				}
			}

		}, null));

		registerDayEvent(WorldEvent.class, new RandomEvent("§a[ Double Money ]", "§7- All enemy money drops are doubled!", (event) -> {
			EnemyDropListener.setDropModifier(2.0f);
		}, (world) -> {
			EnemyDropListener.setDropModifier(1.0f);
		}));
	}

	public static void registerDayEvent(Class<? extends Event> type, RandomEvent event)
	{
		if (!possibleDayEvents.containsKey(type))
		{
			possibleDayEvents.put(type, new ArrayList<>());
		}

		possibleDayEvents.get(type).add(event);
	}

	public static void registerNightEvent(Class<? extends Event> type, RandomEvent event)
	{
		if (!possibleNightEvents.containsKey(type))
		{
			possibleNightEvents.put(type, new ArrayList<>());
		}

		possibleNightEvents.get(type).add(event);
	}

	@EventHandler
	@SuppressWarnings("unchecked")
	public void onNewDay(WorldNewDayEvent event)
	{
		eventHandlers.forEach((type, e) -> e.onConclude(event.getWorld()));
		eventHandlers.clear();

		if (random.nextInt(10) != 0)
		{
			return;
		}

		Class<?>[] possibleKeys = possibleDayEvents.keySet().toArray(new Class<?>[possibleDayEvents.size()]);
		Class<?> type = possibleKeys[random.nextInt(possibleKeys.length)];
		List<RandomEvent> events = possibleDayEvents.get(type);
		RandomEvent chosenEvent = events.get(random.nextInt(events.size()));

		eventHandlers.put((Class<? extends Event>) type, chosenEvent);

		chosenEvent.onChosen();
	}

	@EventHandler
	@SuppressWarnings("unchecked")
	public void onNewNight(WorldNightStartEvent event)
	{
		eventHandlers.forEach((type, e) -> e.onConclude(event.getWorld()));
		eventHandlers.clear();

		if (random.nextInt(10) != 0)
		{
			return;
		}

		Class<?>[] possibleKeys = possibleNightEvents.keySet().toArray(new Class<?>[possibleNightEvents.size()]);
		Class<?> type = possibleKeys[random.nextInt(possibleKeys.length)];
		List<RandomEvent> events = possibleNightEvents.get(type);
		RandomEvent chosenEvent = events.get(random.nextInt(events.size()));

		eventHandlers.put((Class<? extends Event>) type, chosenEvent);

		chosenEvent.onChosen();
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event)
	{
		if (eventHandlers.containsKey(EntitySpawnEvent.class))
		{
			eventHandlers.get(EntitySpawnEvent.class).getEvent().accept(event);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (eventHandlers.containsKey(PlayerMoveEvent.class))
		{
			eventHandlers.get(PlayerMoveEvent.class).getEvent().accept(event);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityMonitor(EntitySpawnEvent event)
	{
		//Logger.debug("Entity spawn: " + event.getEntityType().name() + ", cancelled: " + event.isCancelled());
	}

	@EventHandler
	public void onCropGrow(BlockGrowEvent event)
	{
		if (!CROPS.contains(event.getBlock().getType()))
		{
			return;
		}

		if (event.getBlock().getWorld().hasStorm())
		{
			if (!(event.getNewState().getBlockData() instanceof Ageable))
			{
				return;
			}

			Ageable data = (Ageable) event.getNewState().getBlockData();
			if (data.getAge() < data.getMaximumAge())
			{
				data.setAge(data.getAge() + 1);
				event.getNewState().setBlockData(data);
			}
		}
	}
}
