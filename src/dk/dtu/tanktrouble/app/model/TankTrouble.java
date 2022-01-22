package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.app.controller.sprites.TankMath;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import dk.dtu.tanktrouble.app.model.powerups.PowerUp;
import dk.dtu.tanktrouble.app.model.powerups.TankMod;
import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.server.PlayerBroadcaster;
import dk.dtu.tanktrouble.data.GameSnapshot;
import dk.dtu.tanktrouble.data.Player;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.records.BulletRecord;
import dk.dtu.tanktrouble.data.records.PowerUpRecord;
import dk.dtu.tanktrouble.data.records.TankRecord;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dk.dtu.tanktrouble.app.model.Bullet.TIME_S;

public class TankTrouble extends GameLoop {

	long msUntilGameOver, msUntilNextPowerUp;

	final PlayerBroadcaster broadcaster;

	public GameMap map;
	public List<Tank> tanks;
	final List<Bullet> bullets;
	final List<PowerUp> powerUps;
	List<Class<? extends TankMod>> activeMods;

	private boolean hasUpdatedScore = false;

	private static final int MIN_WIDTH = 4, MAX_WIDTH = 10, MIN_HEIGHT = 4, MAX_HEIGHT = 8;
	private static final int MIN_SIZE = 30, MAX_SIZE = 72;
	private static final long GAME_OVER_DELAY = 2000, POWER_UP_MIN_DELAY = 7500, POWER_UP_MAX_DELAY = 15000;

	public TankTrouble(PlayerBroadcaster broadcaster) {
		this.broadcaster = broadcaster;
		bullets = new ArrayList<>();
		powerUps = new ArrayList<>();
	}

	public void generateNewLevel(List<Player> players) {
		this.map = generateMap();
		this.tanks = generateTanks(map, players);
		this.bullets.clear();
		this.powerUps.clear();
		msUntilGameOver = GAME_OVER_DELAY;
		hasUpdatedScore = false;
		newDelayToNextPowerUp();
		msUntilNextPowerUp = 1000;
		activeMods = PowerUpData.getActive();
	}

	// Loop
	@Override
	public boolean doTick(long now, boolean sendSnapshot) {
		// Move
		for (Tank tank : tanks) {
			tank.moveKeyPress(tanks, SPT, now);
		}

		// Shoot if alive
		List<Tank> aliveTanks = new ArrayList<>();
		for (Tank tank : tanks) {
			if (tank.isAlive()) {
				if (tank.player.spacePressed) {
					tank.shoot(now);
				}
				aliveTanks.add(tank);
			}
			tank.player.spacePressed = false;
		}

		// Update bullets
		for (Tank tank : tanks) {
			List<Bullet> bullets = tank.bullets;
			for (int i = bullets.size() - 1; i >= 0; i--) {
				Bullet bullet = bullets.get(i);
				bullet.updatePosition(SPT, now);
				bullet.checkCollisionWithTank(aliveTanks);

				if (!bullet.isAlive(now)) {
					bullet.onDeath(now);
					bullets.remove(bullet);

					if (!bullet.isSpecialBullet())
						tank.bulletCount--;
				}
			}
		}

		// alive taks checkup
		for (int i = aliveTanks.size() - 1; i >= 0; i--) {
			Tank checkTank = aliveTanks.get(i);
			if (!checkTank.isAlive()) {
				updateScoreBoardKill(checkTank.killer, checkTank);
				aliveTanks.remove(checkTank);
			}
		}


		// Update powerups
		for (int i = powerUps.size() - 1; i >= 0; i--) {
			PowerUp powerUp = powerUps.get(i);
			Tank tankCollision = powerUp.hitTank(aliveTanks);
			if (tankCollision != null) {
				tankCollision.addMod(powerUp.tankMod);
			}
			if (!powerUp.alive) {
				powerUps.remove(i);
			}
		}

		// Send snapshot
		if (sendSnapshot) {
			long syncedNow = now + TICK_DELAY * TIME_S / TPS;
			List<Bullet> bullets = new ArrayList<>();
			for (Tank tank : tanks)
				bullets.addAll(tank.bullets);

			GameSnapshot gameSnapshot = new GameSnapshot(TankRecord.generateRecords(tanks), BulletRecord.generateRecords(bullets, syncedNow), PowerUpRecord.generateRecords(powerUps), syncedNow);
			try {
				broadcaster.sendToPlayers(Commands.NEW_SNAPSHOT, Commands.serialize(gameSnapshot));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Timing
		msUntilNextPowerUp -= MSPT;
		if (msUntilNextPowerUp < 0) {
			PowerUp p = generatePowerUp();
			if (p != null) {
				powerUps.add(p);
				msUntilNextPowerUp += newDelayToNextPowerUp();
			}
		}

		// Game Over Condition
		if (aliveTanks.size() <= 1 && aliveTanks.size() < tanks.size()) {
			msUntilGameOver -= MSPT;
			if (!hasUpdatedScore) {
				for (Tank winnerTank : aliveTanks) {
					updateScoreBoardWin(winnerTank);
				}
				hasUpdatedScore = true;
			}
		}
		return msUntilGameOver > 0;
	}

	private void updateScoreBoardWin(Tank winner) {
		ScoringSystem.rewardWinner(winner.player);
		updateScoreBoard();
	}

	private void updateScoreBoardKill(Tank killer, Tank killed) {
		ScoringSystem.rewardKill(killer.player);
		ScoringSystem.penalizeKilled(killed.player);
		updateScoreBoard();
	}

	private void updateScoreBoard() {
		ArrayList<Player> players = new ArrayList<>();

		for (Tank tank : this.tanks) {
			players.add(tank.player);
		}
		try {
			broadcaster.sendToPlayers(Commands.PLAYERS_UPDATED, Commands.serialize(players));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Generation
	private GameMap generateMap() {
		// Generate map
		Random rnd = new Random();
		int width, height;
		do {
			width = rnd.nextInt(MAX_WIDTH - MIN_WIDTH + 1) + MIN_WIDTH;
			height = rnd.nextInt(MAX_HEIGHT - MIN_HEIGHT + 1) + MIN_HEIGHT;
		} while (width * height < MIN_SIZE || width * height > MAX_SIZE || width < height);

		return new GameMap(width, height);
	}

	private List<Tank> generateTanks(GameMap map, List<Player> players) {
		Random rnd = new Random();
		List<Tank> tanks = new ArrayList<>();

		for (Player player : players) {
			Tank tank;
			do {
				tank = new Tank(
						map,
						rnd.nextInt(map.getWidth()) + 0.5,
						rnd.nextInt(map.getHeight()) + 0.5,
						Math.random() * Math.PI * 2, player.getId());

				tank.player = player;
				tank.resurrect();

			} while (tank.isCollidingWithTank(tanks));

			tanks.add(tank);
		}

		return tanks;
	}

	private PowerUp generatePowerUp() {
		if (activeMods.size() == 0) return null;
		Random rnd = new Random();

		double x = rnd.nextInt(map.getWidth()) + 0.5;
		double y = rnd.nextInt(map.getHeight()) + 0.5;
		double angle = rnd.nextDouble() * TankMath.PI2;

		for (Tank tank : tanks)
			if (new Vector2(tank.getX() - x, tank.getY() - y).length() < ((Tank.WIDTH / 2) + .5)) return null;

		for (PowerUp powerUp : powerUps)
			if (new Vector2(powerUp.x - x, powerUp.y - y).length() <= 2) return null;

		int idx = rnd.nextInt(activeMods.size());
		//idx = 3;
		try {
			return new PowerUp(x, y, angle, activeMods.get(idx).getDeclaredConstructor().newInstance());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	private long newDelayToNextPowerUp() {
		return POWER_UP_MIN_DELAY + (long) (new Random().nextDouble() * (POWER_UP_MAX_DELAY - POWER_UP_MIN_DELAY));
	}

}
