package coffee.khyonieheart.brimstone.claiming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.Expose;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ClaimData
{
	@Expose
	private Map<String, Byte> addedPlayers = new HashMap<>();

	@Expose
	private int x, y, z;

	@Expose
	private String world, owner;

	@Expose
	private boolean allowExplosions = false;
	@Expose
	private boolean allowExternalWaterflow = false;

	private static final byte DEFAULT_PERMISSIONS = 0b0011_1100;
	private static final byte OWNER_PERMISSIONS = (byte) 0b1111_1111;

	/* @deprecated Deserialization target */
	@Deprecated
	public ClaimData() 
	{

	}

	public ClaimData(Location location, Player owner)
	{
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		this.world = location.getWorld().getName();
		this.owner = owner.getUniqueId().toString();

		addPlayer(owner, OWNER_PERMISSIONS);
	}

	public String getOwner()
	{
		return this.owner;
	}

	public void addPlayer(Player player)
	{
		addPlayer(player, DEFAULT_PERMISSIONS);
	}

	public boolean overlapsNotOwned()
	{
		for (ClaimData claim : ClaimManager.getClaims())
		{
			if (claim.getOwner().equals(this.getOwner()))
			{
				continue;
			}

			if (Math.abs(claim.x - this.x) < 26 && Math.abs(claim.z - this.z) < 26)
			{
				return true;
			}
		}

		return false;
	}

	public void addPlayer(Player player, byte permissions)
	{
		// We use a bitfield here to cut down on mess
		// 0 = Break blocks
		// 1 = Place blocks
		// 2 = Damage players
		// 3 = Damage entities
		// 4 = Interact interactable (doors, trapdoors)
		// 5 = Interact redstone (buttons, levers)
		// 6 = Interact containers
		// 7 = Break claim
		
		addedPlayers.put(player.getUniqueId().toString(), permissions);
	}

	public void addPlayer(String uuid, byte permissions)
	{
		addedPlayers.put(uuid, permissions);
	}

	public Set<String> getAllPlayers()
	{
		return this.addedPlayers.keySet();
	}

	public boolean togglePermission(Player player, ClaimPermission permission)
	{
		return togglePermission(player.getUniqueId().toString(), permission);
	}

	public boolean togglePermission(String uuid, ClaimPermission permission)
	{
		byte mask = (byte) (1 << permission.getOffset());
		byte permissionData = (byte) (addedPlayers.get(uuid) ^ mask);

		this.addedPlayers.put(uuid, permissionData);

		return (this.addedPlayers.get(uuid) & mask) > 0;
	}

	public boolean isAdded(Player player)
	{
		return addedPlayers.containsKey(player.getUniqueId().toString());
	}

	public boolean isAdded(String uuid)
	{
		return addedPlayers.containsKey(uuid);
	}

	public boolean hasPermission(Player player, ClaimPermission permission)
	{
		return hasPermission(player.getUniqueId().toString(), permission);
	}

	public boolean hasPermission(String uuid, ClaimPermission permission)
	{
		if (!addedPlayers.containsKey(uuid))
		{
			return false;
		}

		byte mask = (byte) (1 << permission.getOffset());

		return (addedPlayers.get(uuid) & mask) != 0;
	}

	public byte getPermissions(Player player)
	{
		if (addedPlayers.containsKey(player.getUniqueId().toString()))
		{
			return addedPlayers.get(player.getUniqueId().toString());
		}

		return (byte) 0;
	}

	public byte getPermissions(String uuid)
	{
		if (addedPlayers.containsKey(uuid))
		{
			return addedPlayers.get(uuid);
		}

		return (byte) 0;
	}

	public void removeExplosionTargets(List<Block> targets)
	{
		targets.removeIf((block) -> contains(block));
	}

	public boolean isClaimBlock(Block block)
	{
		return block.getX() == this.x && block.getY() == this.y && block.getZ() == this.z;
	}

	public Block getClaimBlock()
	{
		return new Location(Bukkit.getWorld(this.world), x, y, z).getBlock();
	}

	public boolean allowsExplosions()
	{
		return this.allowExplosions;
	}

	public boolean allowsExternalLiquidFlow()
	{
		return this.allowExternalWaterflow;
	}

	public void setAllowsExplosions(boolean allow)
	{
		this.allowExplosions = allow;
	}

	public void setAllowsExternalLiquidFlow(boolean allow)
	{
		this.allowExternalWaterflow = allow;
	}

	public boolean contains(Block block)
	{
		Location loc = block.getLocation();

		if (!loc.getWorld().getName().equals(this.world))
		{
			return false;
		}

		if (loc.getBlockX() > this.x + 12 || loc.getBlockX() < this.x - 12)
		{
			return false;
		}

		if (loc.getBlockZ() > this.z + 12 || loc.getBlockZ() < this.z - 12)
		{
			return false;
		}

		return true;
	}
}
