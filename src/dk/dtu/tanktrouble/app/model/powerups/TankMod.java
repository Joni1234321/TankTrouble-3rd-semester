package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Tank;

import java.util.List;

import static dk.dtu.tanktrouble.app.controller.sprites.TankMath.PI2;


public abstract class TankMod {
	private int bulletShot = 0;

	public final void shoot(Tank tank, long now) {
		if (tank == null) return;

		spawnBullet(tank, now);
		bulletShot++;

		if (removeModAfterShooting())
			tank.nextMod();
	}

	public void move(Tank tank, List<Tank> tanks, double deltaTime, long now) {
		move(tank, tank.player.upPressed, tank.player.rightPressed, tank.player.downPressed, tank.player.leftPressed, tanks, deltaTime);
	}

	// OVERRIDEABLE
	private void move(Tank tank, boolean up, boolean right, boolean down, boolean left, List<Tank> tanks, double deltaTime) {
		double distX = Math.cos(tank.getAngle()) * Tank.MOVE_SPEED * deltaTime;
		double distY = Math.sin(tank.getAngle()) * Tank.MOVE_SPEED * deltaTime;

		int moveDirection = (up ? 1 : 0) + (down ? -1 : 0);
		int angleDirection = (right ? 1 : 0) + (left ? -1 : 0);

		double nextX = tank.getX() + distX * moveDirection;
		double nextY = tank.getY() + distY * moveDirection;

		double newAngle = clampAngle(tank.getAngle() + angleDirection * Tank.ANGLE_SPEED * deltaTime);

		tank.tryMoveTo(nextX, nextY, newAngle, tanks);
	}

	protected abstract void spawnBullet(Tank tank, long now);

	protected abstract int getTotalShots();

	protected boolean removeModAfterShooting() {
		return (bulletShot >= getTotalShots());
	}

	private double clampAngle(double newAngle) {
		return (((newAngle % PI2) + PI2) % PI2);  // positive modulo: https://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	}
}
