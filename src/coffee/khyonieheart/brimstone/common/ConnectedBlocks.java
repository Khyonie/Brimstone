package coffee.khyonieheart.brimstone.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import coffee.khyonieheart.brimstone.claiming.ClaimData;
import coffee.khyonieheart.brimstone.claiming.ClaimManager;
import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import coffee.khyonieheart.hyacinth.util.marker.Nullable;

public class ConnectedBlocks
{
	public static ConnectedBlocksResult getConnectedBlocks(
		@NotNull Block seed,
		@NotNull Predicate<Block> filter,
		@Nullable Predicate<Block> closedFilter
	) {
		Deque<Block> open = new ArrayDeque<>();
		List<Block> closed = new ArrayList<>();
		List<Block> secondaryClosed = new ArrayList<>();

		open.add(seed);

		Block target;
		Block relativeTarget;
		Location targetLocation;
		final World world = seed.getWorld();
		while (!open.isEmpty())
		{
			target = open.removeLast();
			closed.add(target);
			targetLocation = target.getLocation().clone();

			for (int y = -1; y < 2; y++)
			{
				for (int x = -1; x < 2; x++)
				{
					for (int z = -1; z < 2; z++)
					{
						relativeTarget = world.getBlockAt(targetLocation.clone().add(x, y, z));

						if (target.equals(relativeTarget))
						{
							continue;
						}

						if (relativeTarget == null)
						{
							continue;
						}

						if (open.contains(relativeTarget) || closed.contains(relativeTarget))
						{
							continue;
						}

						if (filter.test(relativeTarget))
						{
							open.push(relativeTarget);
							continue;
						}

						if (closedFilter == null)
						{
							continue;
						}

						if (closedFilter.test(relativeTarget))
						{
							secondaryClosed.add(relativeTarget);
						}
					}
				}
			}
		}

		for (ClaimData data : ClaimManager.getClaims())
		{
			data.removeExplosionTargets(closed);
			data.removeExplosionTargets(secondaryClosed);
		}

		return new ConnectedBlocksResult(closed, secondaryClosed);
	}
}
