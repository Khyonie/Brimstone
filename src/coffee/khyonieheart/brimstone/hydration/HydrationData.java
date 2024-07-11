package coffee.khyonieheart.brimstone.hydration;

import java.util.Map;

import coffee.khyonieheart.crafthyacinth.data.CastableHashMap;
import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;

@FeatureIdentifier({ "hydration" })
public class HydrationData implements Feature
{
	private static boolean enableHydration = false;
	public static CastableHashMap<String, Object> initDefault() 
	{
		return new CastableHashMap<>(
			Map.of(
				"hydrationLevel", 1.2,
				"treecapEnabled", true,
				"hydrationHudType", "bar", // "bar", "percent", "value", "bar+percent"
				"money", 0,
				"isHomed", false
			)
		);	
	}

	public static boolean isHydrationEnabled()
	{
		return enableHydration;
	}

	@Override
	public boolean isEnabled(String target) 
	{
		return switch (target)
		{
			case "hydration" -> enableHydration;
			default -> false;
		};
	}

	@Override
	public boolean kill(String target) 
	{
		return switch (target)
		{
			case "hydration" -> {
				if (enableHydration)
				{
					yield !(enableHydration = false);
				}

				yield false;
			}
			default -> false;
		};
	}

	@Override
	public boolean reenable(String target) 
	{
		return switch (target)
		{
			case "hydration" -> {
				if (!enableHydration) 
				{
					yield enableHydration = true;
				}

				yield false;
			}
			default -> false;
		};
	}
}
