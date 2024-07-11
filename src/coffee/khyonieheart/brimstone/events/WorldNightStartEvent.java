package coffee.khyonieheart.brimstone.events;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;

public class WorldNightStartEvent extends WorldEvent
{
	private static HandlerList HANDLERS = new HandlerList();

	public WorldNightStartEvent(World world)
	{
		super(world);
	}

	@Override
	public HandlerList getHandlers() 
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}
}
