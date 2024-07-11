package coffee.khyonieheart.brimstone.pylon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.Expose;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import coffee.khyonieheart.hyacinth.util.marker.Nullable;

public class Pylon 
{
	private Set<Block> ownedBlocks = new HashSet<>();

	@Expose
	private int x, y, z;
	@Expose
	private boolean publicAccess = true;
	@Expose
	private String worldName, ownedName, ownedUuid, pylonName, iconMaterial;
	@Expose 
	private List<String> homedPlayers, addedPlayers;

	/**
	 * Deserialization target, do not use
	 */
	@Deprecated
	public Pylon()
	{
		
	}

	public Pylon(Block bell, Player owner)
	{
		// Add block to set
		ownedBlocks.add(bell);

		// Block below
		Block current = bell.getRelative(BlockFace.DOWN);
		ownedBlocks.add(current);

		// Cross
		current = current.getRelative(BlockFace.DOWN);
		ownedBlocks.add(current);

		ownedBlocks.add(current.getRelative(BlockFace.NORTH));
		ownedBlocks.add(current.getRelative(BlockFace.EAST));
		ownedBlocks.add(current.getRelative(BlockFace.SOUTH));
		ownedBlocks.add(current.getRelative(BlockFace.WEST));

		// Register user data
		this.ownedName = owner.getDisplayName();
		this.ownedUuid = owner.getUniqueId().toString();
		this.pylonName = "Pylon " + PylonManager.getPylons().size();
		this.iconMaterial = "BELL";

		this.addedPlayers = new ArrayList<>();
		this.addedPlayers.add(owner.getUniqueId().toString());
		this.homedPlayers = new ArrayList<>();

		// Store location
		Location loc = bell.getLocation();
		this.worldName = loc.getWorld().getName();
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
	}

	public void update()
	{
		if (homedPlayers == null)
		{
			homedPlayers = new ArrayList<>();
		}

		if (addedPlayers == null)
		{
			addedPlayers = new ArrayList<>();
			addedPlayers.add(ownedUuid);
		}
	}

	public String getOwnerName()
	{
		return this.ownedName;
	}

	public String getOwnerUuid()
	{
		return this.ownedUuid;
	}

	public String getName()
	{
		return this.pylonName;
	}

	public Material getIcon()
	{
		try {
			return Material.valueOf(this.iconMaterial);
		} catch (IllegalArgumentException e) {
			return Material.BELL;
		}
	}

	private static Set<BlockFace> compassDirections = Set.of(
		BlockFace.NORTH,
		BlockFace.EAST,
		BlockFace.SOUTH,
		BlockFace.WEST
	);

	public Location getLocation(@Nullable Vector lookVector)
	{
		Location loc = new Location(Bukkit.getWorld(this.worldName), this.x, this.y, this.z).add(0.5, 0, 0.5);

		if (lookVector != null)
		{
			loc.setDirection(lookVector);
		}

		return loc;
	}

	public boolean isPublic()
	{
		return this.publicAccess;
	}

	public List<String> getAddedPlayers()
	{
		return this.addedPlayers;
	}

	public List<String> getHomedPlayers()
	{
		return this.homedPlayers;
	}

	public void addHomedPlayer(Player player)
	{
		this.homedPlayers.add(player.getUniqueId().toString());
	}

	public boolean addPlayer(Player player)
	{
		String uuid = player.getUniqueId().toString();

		if (addedPlayers.contains(uuid))
		{
			return false;
		}

		return addedPlayers.add(uuid); // Always true, see Collection#add(E e)
	}

	public boolean removePlayer(Player player)
	{
		String uuid = player.getUniqueId().toString();

		return addedPlayers.remove(uuid);
	}

	public boolean setPrivacy(boolean publicAccess)
	{
		boolean oldAccess = this.publicAccess;

		this.publicAccess = publicAccess;

		return oldAccess;
	}

	public boolean setAsHome(Player player)
	{
		if (homedPlayers.contains(player.getUniqueId().toString()))
		{
			return false;
		}

		player.setBedSpawnLocation(this.getLocation(null), true);
		return true;
	}

	public String setName(String name)
	{
		String oldName = this.pylonName;
		this.pylonName = name;

		return oldName;
	}

	public String setIcon(String icon)
	{
		String oldIcon = this.iconMaterial;
		this.iconMaterial = icon;

		return oldIcon;
	}

	public boolean isHomePylon(Player player)
	{
		return this.homedPlayers.contains(player.getUniqueId().toString());
	}

	protected void loadPylonStructure()
	{
		Block seed = Bukkit.getWorld(this.worldName).getBlockAt(this.x, this.y, this.z);
		ownedBlocks.add(seed);

		Block current = seed.getRelative(BlockFace.DOWN);
		ownedBlocks.add(current);

		current = current.getRelative(BlockFace.DOWN);
		ownedBlocks.add(current);

		for (BlockFace face : compassDirections)
		{
			ownedBlocks.add(current.getRelative(face));
		}
	}

	public static boolean isPylonStructure(Block bell)
	{
		Block relative = bell.getRelative(BlockFace.DOWN);

		if (!relative.getType().equals(Material.PURPUR_PILLAR))
		{
			return false;
		}

		relative = relative.getRelative(BlockFace.DOWN);

		if (!relative.getType().equals(Material.END_STONE_BRICKS))
		{
			return false;
		}

		for (BlockFace face : compassDirections)
		{
			if (!relative.getRelative(face).getType().equals(Material.END_STONE_BRICKS))
			{
				return false;
			}
		}

		return true;
	}

	public boolean isPylonBlock(Block block)
	{
		return ownedBlocks.contains(block);
	}

	public boolean isOwner(Player player)
	{
		return player.getUniqueId().toString().equals(this.ownedUuid);
	}
}
