package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;

public class TankModDefault extends TankMod {

	@Override
	protected void spawnBullet(Tank tank, long now) {
		if (tank.bulletCount >= tank.maxBullets) return;
		new DefaultBullet(tank, now);
	}

	@Override
	protected int getTotalShots() {
		return Integer.MAX_VALUE;
	}
}

class DefaultBullet extends Bullet {
	public DefaultBullet(Tank tank, long startTime) {
		super(tank, tank.getTurretX(), tank.getTurretY(), tank.getAngle(), startTime, BULLET_SPEED_DEFAULT, BULLET_TIME_DEFAULT, BULLET_SIZE_DEFAULT);
		specialBullet = false;
		tank.bulletCount++;
	}
}
