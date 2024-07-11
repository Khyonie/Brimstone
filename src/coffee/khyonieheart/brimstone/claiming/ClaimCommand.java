package coffee.khyonieheart.brimstone.claiming;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.khyonieheart.brimstone.Brimstone;
import coffee.khyonieheart.brimstone.claiming.gui.ClaimGui;
import coffee.khyonieheart.crafthyacinth.command.HyacinthTabCompleter;
import coffee.khyonieheart.crafthyacinth.command.parser.HyacinthCompletionBranch;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.command.HyacinthCommand;
import coffee.khyonieheart.hyacinth.command.NoSubCommandExecutor;
import coffee.khyonieheart.hyacinth.module.HyacinthModule;

public class ClaimCommand extends HyacinthCommand
{
	public ClaimCommand()
	{
		super("protection", "/protection", null, "p", "claims", "c");

		this.setParser(new HyacinthTabCompleter(this));

		this.getTrees().addRoots((b) -> new HyacinthCompletionBranch(), "trust", "explosions", "externalliquids");

		this.getTrees().getTree("trust").add("<ONLINE_PLAYERS>");
		this.getTrees().getTree("explosions").add("allow");
		this.getTrees().getTree("explosions").add("deny");

		this.getTrees().getTree("externalliquids").add("allow");
		this.getTrees().getTree("externalliquids").add("deny");
		return;
		// CompletionBranch baseEditPerms = this.getTrees().getTree("editperms").add("<ONLINE_PLAYERS>");
		// baseEditPerms.add("add").add("<PERMISSIONS>");
		// baseEditPerms.add("remove").add("<PERMISSIONS>");
		// baseEditPerms.add("flags").add("decimal").add("<INTEGER>", NumberValidator.integerValidator());
		// baseEditPerms.get("flags").add("bits").add("<BINARY>");
	}

	@NoSubCommandExecutor
	public void noSubCommand(CommandSender sender, String[] args)
	{
		for (ClaimData data: ClaimManager.getClaims())
		{
			if (!data.contains(((Player) sender).getLocation().getBlock()))
			{
				continue;
			}

			if (!((Player) sender).getUniqueId().toString().equals(data.getOwner()))
			{
				Message.send(sender, "§7You do not own this claim.");
				return;
			}

			ClaimGui.create((Player) sender, data).open((Player) sender);
			return;
		}

		Message.send(sender, "§7You are not in a claim you own.");
	}

	public void trust(CommandSender sender, String[] args)
	{
		if (args.length < 2)
		{
			Message.send(sender, "§cUsage: /claims trust <PLAYER>");
			return;
		}
		for (ClaimData data : ClaimManager.getClaims())
		{
			if (!data.contains(((Player) sender).getLocation().getBlock()))
			{
				continue;
			}

			if (!((Player) sender).getUniqueId().toString().equals(data.getOwner()))
			{
				Message.send(sender, "§7You do not own this claim.");
				return;
			}

			Player target = Bukkit.getPlayerExact(args[1]);
			if (target == null)
			{
				Message.send(sender, "§cNo such player named \"" + args[1] + "\" is online");
				return;
			}

			if (data.isAdded(target))
			{
				Message.send(sender, "§eThat player has already been trusted in this claim.");
			}

			return;
		}
	}

	public void explosions(CommandSender sender, String[] args)
	{
		if (args.length < 2)
		{
			Message.send(sender, "§cUsage: /claims explosions [ allow | deny ]");
			return;
		}

		ClaimData claim = ClaimManager.getClaim(((Player) sender).getLocation().getBlock());
		if (claim == null)
		{
			Message.send(sender, "§7You are not in a claim you own.");
			return;
		}

		if (!claim.getOwner().equals(((Player) sender).getUniqueId().toString()))
		{
			Message.send(sender, "§7You do not own this claim.");
			return;
		}

		switch (args[1].toLowerCase())
		{
			case "allow" -> {
				if (claim.allowsExplosions())
				{
					Message.send(sender, "§7Nothing changed. This claim already allows explosions.");
					return;
				}

				Message.send(sender, "§7Updated claim rules.");
				claim.setAllowsExplosions(true);
			}
			case "deny" -> {
				if (!claim.allowsExplosions())
				{
					Message.send(sender, "§7Nothing changed. This claim already protects against explosions.");
					return;
				}

				Message.send(sender, "§7Updated claim rules.");
				claim.setAllowsExplosions(false);
			}
			default -> Message.send(sender, "§cUsage: /claims explosions [ allow | deny ]");
		}
	}
	
	public void externalliquids(CommandSender sender, String[] args)
	{
		if (args.length < 2)
		{
			Message.send(sender, "§cUsage: /claims externalliquids [ allow | deny ]");
			return;
		}

		ClaimData claim = ClaimManager.getClaim(((Player) sender).getLocation().getBlock());
		if (claim == null)
		{
			Message.send(sender, "§7You are not in a claim you own.");
			return;
		}

		if (!claim.getOwner().equals(((Player) sender).getUniqueId().toString()))
		{
			Message.send(sender, "§7You do not own this claim.");
			return;
		}

		switch (args[1].toLowerCase())
		{
			case "allow" -> {
				if (claim.allowsExternalLiquidFlow())
				{
					Message.send(sender, "§7Nothing changed. This claim already allows external liquid flow.");
					return;
				}

				Message.send(sender, "§7Updated claim rules.");
				claim.setAllowsExternalLiquidFlow(true);
			}
			case "deny" -> {
				if (!claim.allowsExternalLiquidFlow())
				{
					Message.send(sender, "§7Nothing changed. This claim already protects against external liquid flow.");
					return;
				}

				Message.send(sender, "§7Updated claim rules.");
				claim.setAllowsExternalLiquidFlow(false);
			}
			default -> Message.send(sender, "§cUsage: /claims externalliquids [ allow | deny ]");
		}
	}

	@Override
	public HyacinthModule getModule() 
	{
		return Brimstone.getInstance();
	}
}
