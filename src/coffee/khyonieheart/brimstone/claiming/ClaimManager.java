package coffee.khyonieheart.brimstone.claiming;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.reflect.TypeToken;

import org.bukkit.block.Block;

import coffee.khyonieheart.hyacinth.util.JsonUtils;
import coffee.khyonieheart.hyacinth.util.marker.Nullable;

public class ClaimManager
{
	private static Set<ClaimData> claims = new HashSet<>();
	private static final String FILEPATH = "./BrimstoneData/claims.json";

	public static Set<ClaimData> getClaims()
	{
		return claims;
	}

	@Nullable
	public static ClaimData getClaim(
		Block block
	) {
		for (ClaimData data : claims)
		{
			if (data.contains(block))
			{
				return data;
			}
		}

		return null;
	}

	public static void load()
	{
		File file = new File(FILEPATH);

		if (!file.exists())
		{
			save();
		}

		try {
			claims = JsonUtils.fromJson(FILEPATH, new TypeToken<Set<ClaimData>>() {}.getType());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void save()
	{
		JsonUtils.toFile(FILEPATH, claims);
	}
}
