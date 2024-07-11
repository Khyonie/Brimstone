package coffee.khyonieheart.brimstone.economy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.Message;

public class CombatInstance
{
	private static Map<EntityType, Integer> ENTITY_VALUES = new HashMap<>(Map.of(
		// Low tier
		EntityType.ZOMBIE, 1,
		EntityType.SKELETON, 1,
		EntityType.SPIDER, 1,
		EntityType.HUSK, 1,
		EntityType.STRAY, 1,
		EntityType.SILVERFISH, 1,
		EntityType.CREEPER, 1,
		EntityType.DROWNED, 1,
		EntityType.SLIME, 1,
		EntityType.PHANTOM, 1
	));

	static {
		// Mid tier
		ENTITY_VALUES.putAll(Map.of(
			EntityType.ZOMBIFIED_PIGLIN, 2,
			EntityType.SHULKER, 2,
			EntityType.BLAZE, 2,
			EntityType.MAGMA_CUBE, 2,
			EntityType.PIGLIN, 2,
			EntityType.PIGLIN_BRUTE, 2,
			EntityType.CAVE_SPIDER, 2,
			EntityType.PILLAGER, 2,
			EntityType.WITCH, 2,
			EntityType.WITHER_SKELETON, 2
		));

		ENTITY_VALUES.putAll(Map.of(
			EntityType.RAVAGER, 4,
			EntityType.ILLUSIONER, 3,
			EntityType.EVOKER, 3,
			EntityType.GUARDIAN, 2,
			EntityType.ELDER_GUARDIAN, 4,
			EntityType.WARDEN, 6
		));
	}

	private final int COMBAT_FIELD_RADIUS = 40;

	private int score = 0;
	private int nearbyAttacks = 0;
	private Map<Player, Integer> mobsKilled = new HashMap<>();
	private CombatTier tier = CombatTier.D;
	private Location center;
	private BukkitTask timeoutTask, particleTask, distanceTask;
	private int timeoutValue = CombatTier.D.getTimeout();
	private BossBar scoreBar, timeBar;
	private long starttime;

	private Set<Player> involvedPlayers = new HashSet<>();

	public CombatInstance(Player initiator)
	{
		this.starttime = System.currentTimeMillis();
		timeBar = Bukkit.createBossBar("§fTime remaining", BarColor.WHITE, BarStyle.SEGMENTED_20);
		scoreBar = Bukkit.createBossBar("§fCombat rank: " + this.tier.displayName() + " (x" + this.tier.getModifier() + ")", this.tier.getBarColor(), BarStyle.SEGMENTED_20);
		scoreBar.setProgress(0.0);

		addPlayer(initiator);
		setCenter(initiator.getLocation());

		timeoutTask = Bukkit.getScheduler().runTaskTimer(Hyacinth.getInstance(), () -> {
			timeBar.setProgress((1.0 - ((double) (tier.getTimeout() - timeoutValue) / tier.getTimeout())));
			if (decrementTimer())
			{
				concludeCombat();
			}
		}, 60l, 20l);

		particleTask = Bukkit.getScheduler().runTaskTimer(Hyacinth.getInstance(), () -> {
			drawCircle(120, COMBAT_FIELD_RADIUS);
		}, 60l, 2l);

		distanceTask = Bukkit.getScheduler().runTaskTimer(Hyacinth.getInstance(), () -> {
			Set<Player> distantPlayers = null;
			for (Player p : involvedPlayers)
			{
				if (!containsCircular(p.getLocation(), COMBAT_FIELD_RADIUS))
				{
					if (distantPlayers == null)
					{
						distantPlayers = new HashSet<>();
						distantPlayers.add(p);
					}
				}
			}

			if (distantPlayers != null)
			{
				for (Player p : distantPlayers)
				{
					removePlayer(p);
				}
			}

			if (involvedPlayers.isEmpty())
			{
				concludeCombat();
			}
		}, 1L, 100L);
	}

	private boolean decrementTimer()
	{
		return (--timeoutValue) <= 0;
	}

	public Set<Player> getInvolvedPlayers()
	{
		return this.involvedPlayers;
	}

	public void addPlayerKill(Player player)
	{
		if (!mobsKilled.containsKey(player))
		{
			mobsKilled.put(player, 0);
		}

		mobsKilled.put(player, mobsKilled.get(player) + 1);
	}

	public boolean containsCircular(Location location, int radius)
	{
		double distSqrd = Math.pow(location.getX() - center.getX(), 2) + Math.pow(location.getZ() - center.getZ(), 2);

		return distSqrd <= Math.pow(radius, 2);
	}

	public boolean contains(Location location, int radius)
	{
		if (location.getX() > (center.getX() + radius) || location.getX() < (center.getX() - radius))
		{
			return false;
		}

		if (location.getZ() > (center.getZ() + radius) || location.getZ() < (center.getZ() - radius))
		{
			return false;
		}

		return true;
	}

	public boolean checkAntiFarming(Location location)
	{
		if (contains(location, 3))
		{
			nearbyAttacks++;

			return nearbyAttacks > 20;
		}

		nearbyAttacks = 0;
		return false;
	}

	public void addPlayer(Player player)
	{
		involvedPlayers.add(player);
		this.timeBar.addPlayer(player);
		this.scoreBar.addPlayer(player);
	}

	public void removePlayer(Player player)
	{
		this.involvedPlayers.remove(player);
		this.timeBar.removePlayer(player);
		this.scoreBar.removePlayer(player);

		EnemyDropListener.removePlayerFromCombat(player);

		Message.send(player, "§7You have left combat.");
	}

	public void setCenter(Location center)
	{
		this.center = center;
	}

	public int addScore(EntityType type)
	{
		if (!ENTITY_VALUES.containsKey(type))
		{
			return score;
		}

		score += ENTITY_VALUES.get(type);

		if (score >= this.tier.getTarget())
		{
			this.score -= this.tier.getTarget();
			this.tier = this.tier.getNextTier();
			
			scoreBar.setColor(this.tier.getBarColor());
			scoreBar.setTitle("§fCombat rank: " + this.tier.displayName() + " (x" + this.tier.getModifier() + ")");
		}

		timeoutValue = this.tier.getTimeout();

		scoreBar.setProgress((double) this.score / this.tier.getTarget());

		return score;
	}

	public int getScore()
	{
		return this.score;
	}

	public CombatTier getCurrentTier()
	{
		return this.tier;
	}

	public void concludeCombat()
	{
		scoreBar.removeAll();
		timeBar.removeAll();

		this.timeoutTask.cancel();
		this.particleTask.cancel();
		this.distanceTask.cancel();

		if (this.tier != CombatTier.D)
		{
			involvedPlayers.forEach((p) -> {
				Message.send(p, "§f-------------< §7Combat results §f>------------");
				Message.send(p, "§8> Time elapsed: " + timeDeltaToString());

				int finalScore = 0;
				for (CombatTier t : CombatTier.values())
				{
					if (t == this.tier)
					finalScore += t.getTarget();
				}
				finalScore += this.score;
				Message.send(p, "§8> Final score: " + finalScore);

				int kills = 0;
				for (int v : mobsKilled.values())
				{
					kills += v;
				}

				Message.send(p, "§8> Total kills: " + kills);

				Player mvp = null;
				for (Player pl : mobsKilled.keySet())
				{
					if (mvp == null)
					{
						mvp = pl;
						continue;
					}

					if (mobsKilled.get(pl) > mobsKilled.get(mvp))
					{
						mvp = pl;
						continue;
					}
				}

				Message.send(p, "§8> MVP: " + mvp.getDisplayName() + " (" + mobsKilled.get(mvp) + " kills)");
			});
		}

		for (Player p : involvedPlayers)
		{
			EnemyDropListener.removePlayerFromCombat(p);
		}

		involvedPlayers.clear();
	}

	private static final long ONE_SECOND = 1000;
	private static final long ONE_MINUTE = ONE_SECOND * 60;
	private static final long ONE_HOUR = ONE_MINUTE * 60;

	private String timeDeltaToString()
	{
		int hours = 0;
		int minutes = 0;
		int seconds = 0;

		long timeDelta = System.currentTimeMillis() - this.starttime;
		while (timeDelta >= ONE_HOUR)
		{
			timeDelta -= ONE_HOUR;
			hours++;
		}

		while (timeDelta >= ONE_MINUTE)
		{
			timeDelta -= ONE_MINUTE;
			minutes++;
		}

		while (timeDelta >= ONE_SECOND)
		{
			timeDelta -= ONE_SECOND;
			seconds++;
		}

		return hours + ":" + minutes + ":" + seconds + "." + timeDelta;
	}

	private void drawCircle(
		int points,
		float radius
	) {
		double x, z;
		double angleIncrement = (2 * Math.PI) / points;

		for (int i = 0; i < points; i++)
		{
			double angle = i * angleIncrement;

			x = this.center.getX() + (radius * Math.cos(angle));
			z = this.center.getZ() + (radius * Math.sin(angle));

			Location location = new Location(center.getWorld(), x, 0, z);
			location.setY(center.getWorld().getHighestBlockYAt((int) x, (int) z) + 1.5);
			center.getWorld().spawnParticle(Particle.REDSTONE, location, 1, new DustOptions(Color.RED, 1));
		}
	}
}
