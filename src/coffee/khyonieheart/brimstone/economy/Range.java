package coffee.khyonieheart.brimstone.economy;

import java.util.Random;

public class Range
{
	private int minimum, maximum;
	private Random random = new Random();

	public Range(int minimum, int maximum)
	{
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public int getRandom()
	{
		return minimum + (random.nextInt(maximum) + 1 - minimum);
	}
}
