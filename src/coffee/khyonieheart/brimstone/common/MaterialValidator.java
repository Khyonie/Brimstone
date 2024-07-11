package coffee.khyonieheart.brimstone.common;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import coffee.khyonieheart.hyacinth.command.parser.CompletionBranch;
import coffee.khyonieheart.hyacinth.command.parser.ExecutionValidator;
import coffee.khyonieheart.hyacinth.command.parser.ValidatorContext;
import coffee.khyonieheart.hyacinth.option.Option;
import coffee.khyonieheart.hyacinth.util.Arrays;

public class MaterialValidator implements ExecutionValidator
{
	@Override
	public Option validate(CommandSender sender, ValidatorContext context, CompletionBranch branch, String argument, int argumentIndex, String commandLabel, String[] args) 
	{
		try {
			Material.valueOf(argument);
			return Option.none();
		} catch (IllegalArgumentException e) {
			return switch (context)
			{
				case EXECUTION -> {
					args[argumentIndex] = "§n" + args[argumentIndex] + "§r§c (← Here)";
					yield Option.some("§cInvalid material \"" + argument + "\" in \"/" + commandLabel + " " + Arrays.toString(args, " ", null) + "\".");
				}
				case TABCOMPLETE -> Option.some("§c(⚠ Invalid material \"" + argument + "\")");
			};
		}
	}
}
