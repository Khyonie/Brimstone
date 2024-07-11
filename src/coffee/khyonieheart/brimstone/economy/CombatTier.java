package coffee.khyonieheart.brimstone.economy;

import org.bukkit.boss.BarColor;

public enum CombatTier
{
	D("D", BarColor.RED, 1.0f, 0.10, 5, 15, 1),
	C("C", BarColor.RED, 1.2f, 0.10, 8, 15, 2),
	B("B", BarColor.YELLOW, 1.5f, 0.10, 12, 15, 3),
	A("A", BarColor.YELLOW, 2.0f, 0.15, 20, 15, 4),
	S("S", BarColor.PINK, 3.0f, 0.20, 50, 15, 5),
	S_PLUS("SS", BarColor.PURPLE, 4.5f, 0.33, 60, 15, 6),
	S_PLUS_PLUS("SSS", BarColor.PINK, 10.0f, 0.50, 80, 15, 7),
	S_STAR("S★", BarColor.WHITE, 50.0f, 1.0, Integer.MAX_VALUE, 15, 0)
	;

	private String display;
	private BarColor barColor;
	private float modifier;
	private double dropPercent;
	private int target;
	private int timeout;
	private int nextTier;

	private CombatTier(
		String display, 
		BarColor color, 
		float modifier, 
		double dropPercent,
		int target, 
		int timeout, 
		int nextTier
	) {
		this.display = display;
		this.barColor = color;
		this.modifier = modifier;
		this.dropPercent = dropPercent;
		this.target = target;
		this.timeout = timeout;
		this.nextTier = nextTier;
	}

	public String displayName()
	{
		return this.display;
	}

	public BarColor getBarColor()
	{
		return this.barColor;
	}

	public float getModifier()
	{
		return this.modifier;
	}

	public double getDropPercent()
	{
		return this.dropPercent;
	}

	public int getTarget()
	{
		return this.target;
	}

	public int getTimeout()
	{
		return this.timeout;
	}

	public CombatTier getNextTier()
	{
		return values()[this.nextTier];
	}
}
