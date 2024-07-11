package coffee.khyonieheart.brimstone.claiming;

public enum ClaimPermission
{
	BREAK_BLOCKS(0, "Break blocks: "),
	PLACE_BLOCKS(1, "Place blocks: "),
	DAMAGE_PLAYERS(2, "Attack players: "),
	DAMAGE_ENTITIES(3, "Attack mobs: "),
	INTERACT_SIMPLE(4, "Interact with blocks: "),
	INTERACT_REDSTONE(5, "Interact with redstone: "),
	INTERACT_CONTAINERS(6, "Interact with containers: "),
	BREAK_CLAIM(7, "Remove claim:§8 ")
	;

	private int offset;
	private String displayName;

	private ClaimPermission(int offset, String displayName)
	{
		this.offset = offset;
		this.displayName = displayName;
	}

	public byte getOffset()
	{
		return (byte) offset;
	}

	public String displayName()
	{
		return this.displayName;
	}
}
