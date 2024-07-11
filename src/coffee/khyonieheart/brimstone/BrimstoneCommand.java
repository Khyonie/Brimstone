package coffee.khyonieheart.brimstone;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.khyonieheart.brimstone.common.MaterialValidator;
import coffee.khyonieheart.brimstone.economy.Transactions;
import coffee.khyonieheart.brimstone.events.DailyEventHandler;
import coffee.khyonieheart.brimstone.pylon.Pylon;
import coffee.khyonieheart.brimstone.pylon.PylonManager;
import coffee.khyonieheart.brimstone.time.TimeManager;
import coffee.khyonieheart.crafthyacinth.command.HyacinthTabCompleter;
import coffee.khyonieheart.crafthyacinth.command.parser.HyacinthCompletionBranch;
import coffee.khyonieheart.crafthyacinth.command.parser.validator.CommandValidator;
import coffee.khyonieheart.crafthyacinth.command.parser.validator.NumberValidator;
import coffee.khyonieheart.crafthyacinth.command.parser.validator.PermissionValidator;
import coffee.khyonieheart.crafthyacinth.command.parser.validator.UserValidator;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.command.HyacinthCommand;
import coffee.khyonieheart.hyacinth.command.SubcommandPrefix;
import coffee.khyonieheart.hyacinth.module.HyacinthModule;
import coffee.khyonieheart.hyacinth.util.CastableMap;

@SubcommandPrefix("brimstone_")
public class BrimstoneCommand extends HyacinthCommand
{
	public BrimstoneCommand() 
	{
		super("brimstone", "/brimstone", null, "brim");

		this.getTrees().addRoots((branch) -> { return new HyacinthCompletionBranch(); }, "starttime", "stoptime", "settime", "sethydration", "treecap", "queryevents", "checksolid", "setbalance", "pay", "pylons");

		this.getTrees().getTree("treecap").addValidator(UserValidator.isPlayer());
		this.getTrees().getTree("checksolid").add("<MATERIAL>", CommandValidator.argsCount(2), new MaterialValidator());
		this.getTrees().getTree("pay").addValidator(CommandValidator.argsCount(3));
		this.getTrees().getTree("pay").add("<ONLINE_PLAYERS>").add("<MONEY_TO_SEND>", NumberValidator.integerValidator());

		this.getTrees().getTree("pylons").add("<ADDED_PYLONS>").add("seticon").add("<MATERIAL>", new MaterialValidator());
		this.getTrees().getTree("pylons").get("<ADDED_PYLONS>").add("setname").add("<NAME>");
		this.getTrees().getTree("pylons").get("<ADDED_PYLONS>").add("add").add("<ONLINE_PLAYERS>");
		this.getTrees().getTree("pylons").get("<ADDED_PYLONS>").add("remove").add("<ONLINE_PLAYERS>");
		this.getTrees().getTree("pylons").get("<ADDED_PYLONS>").add("setpublic").add("<BOOLEAN>");

		this.getTrees().getTree("setbalance").addValidator(new PermissionValidator("brimstone.admin", false));
		this.getTrees().getTree("starttime").addValidator(new PermissionValidator("brimstone.admin", false));
		this.getTrees().getTree("stoptime").addValidator(new PermissionValidator("brimstone.admin", false));
		this.getTrees().getTree("settime").addValidator(new PermissionValidator("brimstone.admin", false));
		this.getTrees().getTree("sethydration").addValidator(new PermissionValidator("brimstone.admin", false));

		this.getTrees().getTree("starttime").add("<TIME_PERIOD>", NumberValidator.longValidator(), CommandValidator.argsCount(2));
		this.getTrees().getTree("settime").add("<NEW_TIME>", NumberValidator.longValidator(), CommandValidator.argsCount(2));
		this.getTrees().getTree("sethydration").add("<NEW_HYDRATION>", NumberValidator.doubleValidator(), UserValidator.isPlayer(), CommandValidator.argsCount(2));
		this.getTrees().getTree("setbalance").add("<ONLINE_PLAYERS>").add("<NEW_BALANCE>", NumberValidator.integerValidator());

		this.setParser(new HyacinthTabCompleter(this));
	}

	public void brimstone_pylons(CommandSender sender, String[] args) // TODO Back to Tidal we go. Have to add support for various syntax
	{
		Player player = (Player) sender;
		Pylon pylon = PylonManager.getPylonByName(player, args[1]);

		if (pylon == null)
		{
			Message.send(sender, "§cNo such pylon \"" + pylon + "\".");
			return;
		}


		if (!pylon.getAddedPlayers().contains(player.getUniqueId().toString()))
		{
			Message.send(sender, "§cYou do not have permission to modify this pylon.");
			return;
		}

		switch (args[2])
		{
			case "seticon" -> {
				pylon.setIcon(args[3]);
				Message.send(sender, "§aChanged pylon icon.");
			}
			case "setname" -> {
				// Search for duplicates
				Pylon duplicatePylon = PylonManager.getPylonByName(player, args[3]);

				if (duplicatePylon != null)
				{
					if (duplicatePylon.isPublic() && pylon.isPublic())
					{
						Message.send(player, "§eAnother public pylon already has that name. You can use this name if your pylon is private.");
						return;
					}

					if (!duplicatePylon.isPublic() && !pylon.isPublic())
					{
						Message.send(player, "§eYou already have a private pylon with that name. You can use this name if one pylon is public.");
						return;
					}

					// Compatible, change name
				}

				pylon.setName(args[3]);
				Message.send(sender, "§aChanged pylon name.");
			}
			case "add" -> {
				Player target = Bukkit.getPlayerExact(args[3]);

				if (target == null)
				{
					Message.send(sender, "§cNo player with that name is online.");
					return;
				}

				if (pylon.getAddedPlayers().contains(target.getUniqueId().toString()))
				{
					Message.send(sender, "§eThat player has already been added to this pylon.");
					return;
				}

				pylon.getAddedPlayers().add(target.getUniqueId().toString());
				Message.send(player, "§aAdded " + args[3] + " to this pylon.");
			}
			case "remove" -> {
				Player target = Bukkit.getPlayerExact(args[3]);

				if (target == null)
				{
					Message.send(sender, "§cNo player with that name is online.");
					return;
				}

				if (!pylon.getAddedPlayers().contains(target.getUniqueId().toString()))
				{
					Message.send(sender, "§eThat player has not been added to this pylon.");
					return;
				}
				
				pylon.getAddedPlayers().remove(target.getUniqueId().toString());
				pylon.getHomedPlayers().remove(target.getUniqueId().toString());

				Message.send(player, "§aRemoved " + args[3] + " from this pylon.");
			}
			case "setpublic" -> {
				boolean state = false;

				try {
					state = Boolean.parseBoolean(args[3]);
				} catch (IllegalArgumentException e) {
					Message.send(sender, "§cExpected either true or false, received \"" + args[3] + "\"");
					return;
				}

				if (pylon.isPublic() && state)
				{
					Message.send(sender, "§eThis pylon is already public.");
					return;
				}

				if (!pylon.isPublic() && !state)
				{
					Message.send(sender, "§eThis pylon is already private.");
					return;
				}

				List<Pylon> duplicatePylons = PylonManager.getPylonsMatchingName(player, pylon.getName());

				if (duplicatePylons.size() > 1)
				{
					pylon.setName(pylon.getName() + duplicatePylons.size());

					Message.send(sender, "§eThere already is a public pylon with that name. Your pylon has been renamed to " + pylon.getName() + ".");
				}

				pylon.setPrivacy(state);
				Message.send(sender, "§aUpdated pylon privacy.");
			}
			default -> {
				Message.send(sender, "§cUnknown subcommand \"" + args[2] + "\".");
				return;
			}
		}
	}

	public void brimstone_starttime(CommandSender sender, String[] args)
	{
		if (TimeManager.startTime(Bukkit.getWorld("world"), Long.parseLong(args[1])))
		{
			Message.send(sender, "§7Started lengthened time");
			return;
		}

		Message.send(sender, "§cTime has already been extended.");
	}

	public void brimstone_stoptime(CommandSender sender, String[] args)
	{
		if (TimeManager.stopIncreasedTime(Bukkit.getWorld("world")))
		{
			Message.send(sender, "§7Stopped lengthened time");
			return;
		}

		Message.send(sender, "§cTime is already normal.");
	}

	public void brimstone_settime(CommandSender sender, String[] args)
	{
		long time = Long.parseLong(args[1]);

		TimeManager.setTime(time);
	}

	public void brimstone_sethydration(CommandSender sender, String[] args)
	{
		double hydration = Double.parseDouble(args[1]);

		HyacinthDataManager.get((Player) sender, "Brimstone").put("hydrationLevel", hydration);
	}

	public void brimstone_treecap(CommandSender sender, String[] args)
	{
		boolean oldValue = (boolean) HyacinthDataManager.get((Player) sender, "Brimstone").get("treecapEnabled");

		HyacinthDataManager.get((Player) sender, "Brimstone").put("treecapEnabled", !oldValue);

		if (oldValue)
		{
			Message.send(sender, "§7Disabled treecapitator.");
			return;
		}

		Message.send(sender, "§7Enabled treecapitator.");
	}

	public void brimstone_queryevents(CommandSender sender, String[] args)
	{
		Message.send(sender, "§dThis is a debug command. Include a screenshot of this command when reporting an issue related to events.");
		Message.send(sender, "§dActive event(s):");

		DailyEventHandler.getActiveEvents().forEach((type, event) -> {
			Message.send(sender, "Name: \"" + event.getName() + "\", type: " + type.getSimpleName());
			Message.send(sender, "- " + event.getDescription());
		});
	}

	public void brimstone_checksolid(CommandSender sender, String[] args)
	{
		Material material = Material.valueOf(args[1]);

		Message.send(sender, "§9Material \"" + material.name() + "\" is solid? " + material.isSolid());
	}

	public void brimstone_balance(CommandSender sender, String[] args)
	{
		Message.send(sender, "Your balance: $" + ((CastableMap<String, Object>) HyacinthDataManager.get((Player) sender, Brimstone.getInstance())).getInt("money"));
	}

	public void brimstone_setbalance(CommandSender sender, String[] args)
	{
		String username = args[1];
		Player target = Bukkit.getPlayerExact(username);

		if (target == null)
		{
			Message.send(sender, "§cThat player isn't online!");
			return;
		}

		int value = Integer.parseInt(args[2]);

		HyacinthDataManager.get(target, Brimstone.getInstance()).put("money", value);
		Message.send(sender, "§aTarget's balance updated successfully.");
	}

	public void brimstone_pay(CommandSender sender, String[] args)
	{
		String username = args[1];
		Player onlineTarget = Bukkit.getPlayerExact(username);

		int value = Integer.parseInt(args[2]);

		if (value < 0)
		{
			Message.send(sender, "§cYou can't send a negative payment.");
			return;
		}

		CastableMap<String, Object> data = (CastableMap<String, Object>) HyacinthDataManager.get((Player) sender, Brimstone.getInstance());

		if (data.getInt("money") < value)
		{
			Message.send(sender, "§cYou don't have enough money to send!");
			return;
		}

		data.put("money", data.getInt("money") - value);

		if (onlineTarget == null)
		{
			Transactions.applyToPendingTransaction(username, value);

			Message.send(sender, "§aYou have sent $" + value + " to offline user " + username + ".");
			return;
		}

		CastableMap<String, Object> targetData = (CastableMap<String, Object>) HyacinthDataManager.get(onlineTarget, Brimstone.getInstance());
		targetData.put("money", targetData.getInt("money") + value);

		Message.send(sender, "§aYou have sent $" + value + " to " + username + ".");
		Message.send(onlineTarget, "§aYou have received $" + value + " from " + ((Player) sender).getDisplayName() + ".");
	}

	@Override
	public HyacinthModule getModule() 
	{
		return Brimstone.getInstance();
	}
}
