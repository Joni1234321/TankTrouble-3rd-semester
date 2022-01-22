package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.powerups.sharedbullets.FragmentBullet;
import javafx.scene.paint.Color;

public class TankModGrenade extends TankMod {
	@Override
	protected void spawnBullet(Tank tank, long now) {
		new GrenadeBullet(tank, now);
	}

	@Override
	protected int getTotalShots() {
		return 1;
	}
}

class GrenadeBullet extends Bullet {
	private static final int FRAGMENT_COUNT = 30;

	public GrenadeBullet(Tank tank, long startTime) {
		super(tank, tank.getTurretX(), tank.getTurretY(), tank.getAngle(), startTime, BULLET_SPEED_DEFAULT, BULLET_TIME_DEFAULT / 3, BULLET_SIZE_DEFAULT * 1.75);
	}

	@Override
	public void onDeath(long now) {
		// Explode
		for (int i = 0; i < FRAGMENT_COUNT; i++) {
			new FragmentBullet(tank, pos.x(), pos.y(), Math.random() * Math.PI * 2, now);
		}

		super.onDeath(now);

	}

	// A hack to remove the fade effect
	public double getTillTimeOut(long now) {
		return 1e9;
	}

	@Override
	public Color getColor() {
		Color clr = Color.DARKRED;
		return clr.darker();
	}
}

