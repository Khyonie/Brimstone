package coffee.khyonieheart.brimstone.events;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;

public class WorldNewDayEvent extends WorldEvent
{
	private static final HandlerList HANDLERS = new HandlerList();

	public WorldNewDayEvent(World world) 
	{
		super(world);
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() 
	{
		return HANDLERS;
	}
}
