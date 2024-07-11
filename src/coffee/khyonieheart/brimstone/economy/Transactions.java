package coffee.khyonieheart.brimstone.economy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.google.gson.reflect.TypeToken;

import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;
import coffee.khyonieheart.hyacinth.option.Option;
import coffee.khyonieheart.hyacinth.util.JsonUtils;

@FeatureIdentifier( "pendingTransactions" )
public class Transactions implements Feature
{
	private static Map<String, Integer> pendingTransactions = new HashMap<>();
	private static final String FILEPATH = "./BrimstoneData/transactions.json";

	private static boolean isPendingTransactionEnabled = true;

	public static void load()
	{
		File file = new File(FILEPATH);

		if (!file.exists())
		{
			save();
		}

		try {
			// FIXME This is broken
			// Fix for HashMap<Integer, Integer> error
			try {
				JsonUtils.fromJson(FILEPATH, new TypeToken<HashMap<Integer, Integer>>() {}.getType());

				save();
			} catch (ClassCastException e) {}

			pendingTransactions = JsonUtils.fromJson(FILEPATH, new TypeToken<HashMap<String, Integer>>() {}.getType());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void save()
	{
		JsonUtils.toFile(FILEPATH, pendingTransactions);
	}

	public static void clearPendingTransaction(Player player)
	{
		pendingTransactions.remove(player.getDisplayName());
	}

	public static void applyToPendingTransaction(String name, int value)
	{
		if (!isPendingTransactionEnabled)
		{
			return;
		}

		if (!pendingTransactions.containsKey(name))
		{
			pendingTransactions.put(name, value);
			return;
		}

		pendingTransactions.put(name, pendingTransactions.get(name) + value);
	}

	public static Option getPendingTransaction(Player player)
	{
		if (!isPendingTransactionEnabled)
		{
			return Option.none();
		}

		if (pendingTransactions.containsKey(player.getDisplayName()))
		{
			return Option.some(pendingTransactions.get(player.getDisplayName()));
		}

		return Option.none();
	}

	@Override
	public boolean isEnabled(String target) 
	{
		return switch (target)
		{
			case "pendingTransactions" -> {
				yield isPendingTransactionEnabled;
			}
			default -> {
				yield false;
			}
		};
	}

	@Override
	public boolean kill(String target) 
	{
		switch (target)
		{
			case "pendingTransactions" -> {
				if (isPendingTransactionEnabled)
				{
					isPendingTransactionEnabled = false;
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean reenable(String target) 
	{
		switch (target)
		{
			case "pendingTransactions" -> {
				if (!isPendingTransactionEnabled)
				{
					isPendingTransactionEnabled = true;
					return true;
				}
			}
		}

		return false;
	}
}
