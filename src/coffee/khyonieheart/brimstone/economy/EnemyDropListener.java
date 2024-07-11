package coffee.khyonieheart.brimstone.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import coffee.khyonieheart.crafthyacinth.data.HyacinthDataManager;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.killswitch.Feature;
import coffee.khyonieheart.hyacinth.killswitch.FeatureIdentifier;
import coffee.khyonieheart.hyacinth.util.CastableMap;

@FeatureIdentifier({ "enemyMoneyDrops" })
public class EnemyDropListener implements Listener, Feature
{
	private static float globalDropModifier = 1.0f;
	private static boolean isEnabled = true;
	private static Map<Player, CombatInstance> activeCombatInstances = new HashMap<>();
	private static Random random = new Random();

	private static Map<EntityType, Range> rewardValues = new HashMap<>(Map.of(
		EntityType.PIGLIN, new Range(2, 5),
		EntityType.SPIDER, new Range(1, 3),
		EntityType.ZOMBIFIED_PIGLIN, new Range(2, 3),
		EntityType.BLAZE, new Range(1, 3),
		EntityType.CREEPER, new Range(2, 5),
		EntityType.DROWNED, new Range(1, 3),
		EntityType.ELDER_GUARDIAN, new Range(20, 30),
		EntityType.ENDERMITE, new Range(1, 2),
		EntityType.EVOKER, new Range(5, 6),
		EntityType.GHAST, new Range(4, 8)
	));
	static {
		rewardValues.putAll(Map.of(
			EntityType.GUARDIAN, new Range(3, 10),
			EntityType.HUSK, new Range(1, 3),
			EntityType.MAGMA_CUBE, new Range(1, 2),
			EntityType.PIGLIN_BRUTE, new Range(4, 8),
			EntityType.PILLAGER, new Range(5, 10),
			EntityType.RAVAGER, new Range(15, 25),
			EntityType.SHULKER, new Range(1, 3),
			EntityType.SILVERFISH, new Range(1, 2),
			EntityType.SKELETON, new Range(1, 3),
			EntityType.SLIME, new Range(1, 2)
		));

		rewardValues.putAll(Map.of(
			EntityType.STRAY, new Range(2, 4),
			EntityType.VINDICATOR, new Range(5, 15),
			EntityType.WARDEN, new Range(50, 51),
			EntityType.WITCH, new Range(3, 6),
			EntityType.WITHER_SKELETON, new Range(1, 3),
			EntityType.ZOMBIE, new Range(1, 3),
			EntityType.ZOMBIE_VILLAGER, new Range(1, 3)
		));
	}

	@EventHandler
	public void onDamageKill(EntityDamageByEntityEvent event)
	{
		if (!isEnabled)
		{
			return;
		}

		if (event.isCancelled())
		{
			return;
		}

		if (!rewardValues.containsKey(event.getEntityType()))
		{
			return;
		}

		Player player = null;
		if (!(event.getDamager() instanceof Player))
		{
			if (!(event.getDamager() instanceof Arrow))
			{
				return;
			}

			Projectile projectile = (Projectile) event.getDamager();
			if (!(projectile.getShooter() instanceof Player))
			{
				return;
			}

			player = (Player) projectile.getShooter();
		}

		if (player == null)
		{
			player = (Player) event.getDamager();
		}

		if (!activeCombatInstances.containsKey(player))
		{
			boolean canJoin = false;
			for (CombatInstance c : activeCombatInstances.values())
			{
				if (!c.contains(player.getLocation(), 20))
				{
					continue;
				}
				activeCombatInstances.put(player, c);
				c.addPlayer(player);
				canJoin = true;
				break;
			}

			if (!canJoin)
			{
				activeCombatInstances.put(player, new CombatInstance(player));
			}
		}

		CombatInstance combat = activeCombatInstances.get(player);

		if (combat.checkAntiFarming(event.getEntity().getLocation()))
		{
			return;
		}

		if (event.getEntity() instanceof Slime slime)
		{
			if (slime.getSize() <= 2)
			{
				return;
			}
		}

		if (event.getEntity().getNearbyEntities(3.0, 3.0, 3.0).stream().filter((e) -> { return e instanceof Mob; }).toList().size() > 6)
		{
			return;
		}

		combat.setCenter(event.getEntity().getLocation());

		if (((Damageable) event.getEntity()).getHealth() - event.getFinalDamage() <= 0)
		{
			combat.addScore(event.getEntityType());
			combat.addPlayerKill(player);

			double randomValue = random.nextDouble();
			if (randomValue < combat.getCurrentTier().getDropPercent())
			{
				int value = Math.round(rewardValues.get(event.getEntityType()).getRandom() * globalDropModifier * combat.getCurrentTier().getModifier() * (1.0f + (combat.getInvolvedPlayers().size() * 0.3f)));
				
				for (Player p : combat.getInvolvedPlayers())
				{
					CastableMap<String, Object> data = (CastableMap<String, Object>) HyacinthDataManager.get(player, "Brimstone");
					data.put("money", data.getInt("money") + value);
					Message.send(p, "§7You have been awarded $" + value + ".");
				}
			}
		}
	}

	public static void removePlayerFromCombat(Player player)
	{
		activeCombatInstances.remove(player);
	}

	public static void setDropModifier(float modifier)
	{
		globalDropModifier = modifier;
	}

	@Override
	public boolean isEnabled(
		String target
	) {
		return switch (target) {
			case "enemyMoneyDrops" -> isEnabled;
			default -> false;
		};
	}

	@Override
	public boolean kill(
		String target
	) {
		return switch (target) {
			case "enemyMoneyDrops" -> {
				if (isEnabled)
				{
					yield !(isEnabled = false);
				}

				yield false;
			}
			default -> false;
		};
	}

	@Override
	public boolean reenable(
		String target
	) {
		return switch (target) {
			case "enemyMoneyDrops" -> {
				if (!isEnabled)
				{
					yield isEnabled = true;
				}

				yield false;
			}
			default -> false;	
		};
	}
}
