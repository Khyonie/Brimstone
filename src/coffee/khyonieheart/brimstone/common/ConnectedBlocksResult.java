package coffee.khyonieheart.brimstone.common;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.block.Block;

public class ConnectedBlocksResult
{
	private final List<Block> primaryClosed;
	private final List<Block> secondaryClosed;

	public ConnectedBlocksResult(List<Block> primaryClosed, List<Block> secondaryClosed)
	{
		this.primaryClosed = primaryClosed;
		this.secondaryClosed = secondaryClosed;
	}

	public ConnectedBlocksResult primaryForEach(Consumer<Block> action)
	{
		primaryClosed.forEach((b) -> action.accept(b));

		return this;
	}

	public ConnectedBlocksResult secondaryForEach(Consumer<Block> action)
	{
		secondaryClosed.forEach((b) -> action.accept(b));

		return this;
	}
}
