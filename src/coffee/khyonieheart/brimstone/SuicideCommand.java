package coffee.khyonieheart.brimstone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.command.HyacinthCommand;
import coffee.khyonieheart.hyacinth.command.NoSubCommandExecutor;
import coffee.khyonieheart.hyacinth.module.HyacinthModule;

public class SuicideCommand extends HyacinthCommand
{
	public SuicideCommand() 
	{
		super("suicide", "/suicide", null, "die");
	}

	@NoSubCommandExecutor
	public void execute(CommandSender sender, String[] args)
	{
		if (sender instanceof Damageable p)
		{
			double current = p.getHealth();
			p.damage(current);

			EntityDamageEvent event = new EntityDamageEvent(p, DamageCause.SUICIDE, current);
			event.getEntity().setLastDamageCause(event);
			Bukkit.getPluginManager().callEvent(event);

			p.setHealth(0);

			Message.send(sender, "§7Because dying is a free action.");
		}
	}

	@Override
	public HyacinthModule getModule() 
	{
		return Brimstone.getInstance();
	}

}
