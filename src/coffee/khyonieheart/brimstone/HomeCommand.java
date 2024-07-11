package coffee.khyonieheart.brimstone;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.khyonieheart.brimstone.pylon.Pylon;
import coffee.khyonieheart.brimstone.pylon.PylonManager;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.command.HyacinthCommand;
import coffee.khyonieheart.hyacinth.command.NoSubCommandExecutor;
import coffee.khyonieheart.hyacinth.module.HyacinthModule;
import coffee.khyonieheart.hyacinth.util.CastableMap;

public class HomeCommand extends HyacinthCommand
{
	public HomeCommand()
	{
		super("home", "/home", null);
	}

	@NoSubCommandExecutor
	public void noSubCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player player)
		{
			CastableMap<String, Object> data = (CastableMap<String, Object>) HyacinthDataManager.get(player, "Brimstone");

			if (!data.get("isHomed", Boolean.class))
			{
				Message.send(sender, "§cYou do not have a pylon set as your home.");
				return;
			}

			for (Pylon p : PylonManager.getPylons())
			{
				if (p.isHomePylon(player))
				{
					player.teleport(p.getLocation(player.getLocation().getDirection()));
					player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

					break;
				}
			}
		}
	}

	@Override
	public HyacinthModule getModule() 
	{
		return Brimstone.getInstance();
	}
}
