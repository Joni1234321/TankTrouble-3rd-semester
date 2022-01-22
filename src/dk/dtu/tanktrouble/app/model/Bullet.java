package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public abstract class Bullet {

	public static final double BULLET_SPEED_DEFAULT = Tank.MOVE_SPEED * 1.5, BULLET_SIZE_DEFAULT = 0.035;
	public static final long TIME_S = 1000000000L, BULLET_TIME_DEFAULT = 10 * TIME_S;

	public final double bulletSpeed, bulletSize;
	public final long bulletTime;

	protected boolean alive = true;
	private boolean shooterInvulnerable = true;
	protected boolean specialBullet;

	protected final GameMap map;
	public final Tank tank;
	protected Vector2 pos, dir;
	protected final long startTime;
	public final String id;
	static int currId = 0;

	public Bullet(Tank tank, double x, double y, double angle, long startTime, double bulletSpeed, long bulletTime, double bulletSize) {

		this.map = tank.map;
		this.tank = tank;
		tank.bullets.add(this);

		pos = new Vector2(x, y);
		dir = new Vector2(Math.cos(angle), Math.sin(angle));

		this.startTime = startTime;
		this.bulletSpeed = bulletSpeed;
		this.bulletTime = bulletTime;
		this.bulletSize = bulletSize;

		specialBullet = true;

		id = "Bullet:" + (++currId);
	}

	public void onDeath(long now) {
		alive = false;
	}

	public void onPositionCollision(Vector2 oldDirection, Vector2 newDirection, double t) {
		pos = pos.add(oldDirection.multiply(t));
		dir = newDirection;
	}


	public final void updatePosition(double deltaTime, long now) {
		List<Polygon> wallColliders = new ArrayList<>();
		for (Wall w : map.getNearbyWalls(pos)) wallColliders.add(w.getPolygon());
		updatePosition(wallColliders, deltaTime, now);
	}

	public void updatePosition(List<Polygon> colliders, double deltaTime, long now) {
		Vector2 r = dir.multiply(bulletSpeed * deltaTime);

		double bestT = 2, t;
		Vector2 r2 = null;
		for (Polygon collider : colliders) {
			Object[] newMove = collider.reflect(pos, r);
			if (newMove != null) {
				t = (Double) newMove[0];
				if (t < bestT) {
					bestT = t;
					r2 = (Vector2) newMove[1];
					shooterInvulnerable = false;
				}
			}
		}

		if (r2 != null) { // Has hit wall
			onPositionCollision(r, r2, bestT);
		} else {
			pos = pos.add(r);
		}
	}

	public void checkCollisionWithTank(List<Tank> aliveTanks) {
		// if still inside the shooting tank
		if (shooterInvulnerable() && this.tank.getPolygon().isInside(pos)) {
			return;
		}

		// check if hit a tank
		for (Tank tank : aliveTanks) {
			if (tank.canGetHit() && tank.getPolygon().isInside(pos)) {
				onTankHit(tank, aliveTanks);
				// Can only hit one tank
				return;
			}
		}

		// if not hitting any tank
		shooterInvulnerable = false;
	}

	protected void onTankHit(Tank hitTank, List<Tank> aliveTanks) {
		hitTank.death(this.tank);
		alive = false;
	}

	public double getTillTimeOut(long now) {
		return (double) (bulletTime + startTime - now) / 1e9;
	}

	public boolean isAlive(long now) {
		return (startTime + bulletTime > now) && alive;
	}

	public double getX() {
		return pos.x();
	}

	public double getY() {
		return pos.y();
	}

	public boolean isSpecialBullet() {
		return specialBullet;
	}

	public Color getColor() {
		return Color.BLACK;
	}

	protected boolean shooterInvulnerable() {
		return shooterInvulnerable;
	}
}
