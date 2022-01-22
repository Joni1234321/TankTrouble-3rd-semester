package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;

public class TankModDoubleBullets extends TankMod {

	@Override
	protected void spawnBullet(Tank tank, long now) {
		new DoubleBullet(tank, now);
	}

	@Override
	protected int getTotalShots() {
		return 10;
	}
}

class DoubleBullet extends Bullet {
	public DoubleBullet(Tank tank, long startTime) {
		super(tank, tank.getTurretX(), tank.getTurretY(), tank.getAngle(), startTime, Bullet.BULLET_SPEED_DEFAULT, Bullet.BULLET_TIME_DEFAULT / 2, Bullet.BULLET_SIZE_DEFAULT * .75);
	}
}
