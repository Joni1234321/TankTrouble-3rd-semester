package dk.dtu.tanktrouble.app.model.powerups.sharedbullets;

import dk.dtu.tanktrouble.app.controller.sprites.TankMath;
import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;

import java.util.List;

public abstract class HarmlessAnimationBullet extends Bullet {

	double t = 0;
	protected final Vector2 start, stop;
	protected final double dist;

	public HarmlessAnimationBullet(Tank tank, Vector2 start, Vector2 stop, long startTime, double timeLeft, double bulletSize) {
		super(tank, start.x(), start.y(), 0, startTime, 0, (long) (timeLeft * TIME_S), bulletSize);
		this.start = start;
		this.stop = stop;
		dist = stop.subtract(start).length();

		for (Vector2 v : new Vector2[]{start, stop}) {
			if (v.x() < 0 || v.x() > tank.map.getWidth() || v.y() < 0 || v.y() > tank.map.getHeight()) {
				alive = false;
				break;
			}
		}
	}

	@Override
	public void updatePosition(List<Polygon> colliders, double deltaTime, long now) {
		if (t > 1) alive = false;

		// Animate
		pos = new Vector2(TankMath.lerp(start.x(), stop.x(), t), TankMath.lerp(start.y(), stop.y(), t));


		t += animationSpeed() * deltaTime;
	}

	@Override
	public void checkCollisionWithTank(List<Tank> aliveTanks) {
		// ignore
	}

	@Override
	protected void onTankHit(Tank hitTank, List<Tank> aliveTanks) {
		// ignore
	}


	public double animationSpeed() {
		return 1;
	}

}
