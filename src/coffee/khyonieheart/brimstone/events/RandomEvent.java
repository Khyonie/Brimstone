package coffee.khyonieheart.brimstone.events;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;

import coffee.khyonieheart.hyacinth.util.marker.Nullable;

public class RandomEvent
{
	private Consumer<Event> event;
	private Consumer<World> cleaner;
	private String name;
	private String description;

	public RandomEvent(String name, String description, Consumer<Event> event, @Nullable Consumer<World> cleaner)
	{
		this.event = event;
		this.cleaner = cleaner;

		this.name = name;
		this.description = description;
	}

	public Consumer<Event> getEvent()
	{
		return this.event;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void onChosen()
	{
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.sendMessage(name);
			player.sendMessage(description);
		});
	}
	
	public void onConclude(World world)
	{
		if (cleaner != null)
		{
			cleaner.accept(world);
		}
	}
}
