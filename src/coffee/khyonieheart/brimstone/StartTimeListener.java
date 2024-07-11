package coffee.khyonieheart.brimstone;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import coffee.khyonieheart.brimstone.time.TimeManager;
import coffee.khyonieheart.crafthyacinth.event.ServerFinishLoadingEvent;

public class StartTimeListener implements Listener
{
	@EventHandler
	public void onServerStart(ServerFinishLoadingEvent event)
	{
		TimeManager.startTime(Bukkit.getWorld("world"), 4);
	}
}
