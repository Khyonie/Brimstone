package coffee.khyonieheart.brimstone.common;

import java.util.UUID;

import com.google.gson.annotations.Expose;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

public class OfflinePlayerProfile
{
	@Expose
	String displayName, textureUrl, uuid;

	public OfflinePlayerProfile(Player player)
	{
		PlayerProfile profile = SkinData.getProfile(player);
	
		displayName = player.getDisplayName();
		textureUrl = profile.getTextures().getSkin().toString();
		uuid = player.getUniqueId().toString();
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public String getSkinUrl()
	{
		return this.textureUrl;
	}

	public PlayerProfile toProfile()
	{
		return Bukkit.createPlayerProfile(UUID.fromString(uuid), displayName);
	}
}
