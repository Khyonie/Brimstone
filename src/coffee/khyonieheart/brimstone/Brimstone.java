package coffee.khyonieheart.brimstone;

import coffee.khyonieheart.brimstone.claiming.ClaimManager;
import coffee.khyonieheart.brimstone.claiming.ClaimSpongeItem;
import coffee.khyonieheart.brimstone.common.MaterialGenerator;
import coffee.khyonieheart.brimstone.common.PylonGenerator;
import coffee.khyonieheart.brimstone.common.SkinData;
import coffee.khyonieheart.brimstone.economy.Transactions;
import coffee.khyonieheart.brimstone.hydration.HydrationData;
import coffee.khyonieheart.brimstone.pylon.PylonManager;
import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hyacinth.Logger;
import coffee.khyonieheart.hyacinth.command.parser.SuggestionManager;
import coffee.khyonieheart.hyacinth.module.HyacinthModule;
import coffee.khyonieheart.hyacinth.util.Folders;

public class Brimstone implements HyacinthModule
{
	private static Brimstone instance;

	@Override
	public void onEnable() 
	{
		instance = this;

		Folders.ensureFolder("./BrimstoneData");

		PylonManager.load();
		Transactions.load();
		ClaimManager.load();
		SkinData.load();

		new ClaimSpongeItem();

		SuggestionManager.register("MATERIAL", new MaterialGenerator());	
		SuggestionManager.register("ADDED_PYLONS", new PylonGenerator());

		Logger.log("Half this stuff is deprecated now lmaooooooo");

		HyacinthDataManager.registerDataCreator(this, (player) -> {
			return HydrationData.initDefault();
		});
	}

	@Override
	public void onDisable() 
	{
		PylonManager.save();
		Transactions.save();
		ClaimManager.save();
		SkinData.save();
	}

	public static Brimstone getInstance()
	{
		return instance;
	}
}
