package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;

import java.util.Random;

public class TankModShotgun extends TankMod {
	@Override
	protected void spawnBullet(Tank tank, long now) {
		for (int i = 0; i < 6; i++) {
			double area = .8;
			double angle = tank.getAngle() - area / 2 + new Random().nextDouble() * area;
			new ShotgunBullet(tank, angle, now);
		}
	}

	@Override
	protected int getTotalShots() {
		return 3;
	}
}

class ShotgunBullet extends Bullet {
	public ShotgunBullet(Tank tank, double angle, long startTime) {
		super(tank, tank.getTurretX(), tank.getTurretY(), angle, startTime, Bullet.BULLET_SPEED_DEFAULT * 2, Bullet.BULLET_TIME_DEFAULT / 8, Bullet.BULLET_SIZE_DEFAULT / 2);
	}
}

