package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import dk.dtu.tanktrouble.app.model.powerups.sharedbullets.HarmlessAnimationBullet;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Random;

import static dk.dtu.tanktrouble.app.model.powerups.TankModDeathRay.BULLET_SIZE_RAY;

public class TankModDeathRay extends TankMod {

	public static final double BULLET_SIZE_RAY = Bullet.BULLET_SIZE_DEFAULT * 2, SHOOT_DELAY = 1, BEAM_SHOOT_ANIMATION_TIME = 0.5;

	long triggeredTime = 0;
	boolean shooting = false;

	int n = 0;

	@Override
	public void move(Tank tank, List<Tank> tanks, double deltaTime, long now) {
		if (triggeredTime != 0) {
			// Add animation
			if (timeTillShooting(now) >= 0) {
				if (n++ % 8 == 0) addChargeAnimationBullet(tank, now);
			} else {
				if (!shooting) {
					new DeathRayBullet(tank, now, timeTillShootAnimationOver(now));
					shooting = true;
				} else if (timeTillShootAnimationOver(now) < 0) {
					tank.nextMod();
				}
			}
		} else {
			super.move(tank, tanks, deltaTime, now);
		}
	}

	@Override
	protected void spawnBullet(Tank tank, long now) {
		if (triggeredTime == 0) triggeredTime = now;
	}

	@Override
	protected int getTotalShots() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected boolean removeModAfterShooting() {
		return false;
	}

	void addChargeAnimationBullet(Tank tank, long now) {
		double angle = tank.getAngle() + new Random().nextDouble() * (Math.PI / 2) - Math.PI / 4;
		double dist = new Random().nextDouble() * .6 + .4;

		Vector2 stop = new Vector2(tank.getTurretX(), tank.getTurretY());
		Vector2 start = stop.add(new Vector2(dist * Math.cos(angle), dist * Math.sin(angle)));

		new ChargeBulletAnimation(tank, start, stop, now, timeTillShooting(now));
	}

	private double timeTillShooting(long now) {
		return SHOOT_DELAY + (double) (triggeredTime - now) / 1e9;
	}

	private double timeTillShootAnimationOver(long now) {
		return BEAM_SHOOT_ANIMATION_TIME + SHOOT_DELAY + (double) (triggeredTime - now) / 1e9;
	}
}

class DeathRayBullet extends Bullet {

	boolean beamUsed = false;

	final Vector2 start, stop, dir;
	final double dist;

	public DeathRayBullet(Tank tank, long startTime, double bulletTime) {
		super(tank, tank.getTurretX(), tank.getTurretY(), tank.getAngle(), startTime, 0, (long) (bulletTime * TIME_S), 0);

		start = pos;

		double angle = tank.getAngle();
		dist = getDistanceToMapBorder(start, angle, tank.map);
		dir = new Vector2(dist * Math.cos(angle), dist * Math.sin(angle));
		stop = start.add(dir);
	}

	// Kill
	@Override
	public void checkCollisionWithTank(List<Tank> aliveTanks) {
		if (beamUsed) return;
		Vector2 v = dir.normalize().rot90().multiply(BULLET_SIZE_RAY);

		Polygon hitbox = new Polygon(new Vector2[]{
				stop.subtract(v),
				start.subtract(v),
				start.add(v),
				stop.add(v)
		});


		// Kill everybody in the path
		for (Tank enemyTank : aliveTanks) {
			if (this.tank == enemyTank) continue;
			System.out.println(tank);
			//Object[] newMove = enemyTank.getPolygon().reflect(pos, dir);
			//if (newMove != null)
			if (hitbox.intersect(enemyTank.getPolygon()) || hitbox.isInside(new Vector2(enemyTank.getX(), enemyTank.getY())))
				onTankHit(enemyTank, aliveTanks);
		}

		// make it harmless and animate
		beamUsed = true;
	}

	@Override
	protected void onTankHit(Tank hitTank, List<Tank> aliveTanks) {
		hitTank.death(this.tank);
	}

	// Animate
	int n = 0;

	@Override
	public void updatePosition(List<Polygon> colliders, double deltaTime, long now) {
		// ignore
		if (n++ % 3 == 0) {

			Vector2 middle = start.add(dir.multiply(.5));
			double timeLeft = (double) bulletTime / (double) Bullet.TIME_S;
			new DeathRayBulletAnimation(tank, middle, start, now, timeLeft);
			new DeathRayBulletAnimation(tank, middle, stop, now, timeLeft);
			new DeathRayBulletAnimation(tank, start, stop, now, timeLeft);
			new DeathRayBulletAnimation(tank, stop, start, now, timeLeft);
		}
	}

	double getDistanceToMapBorder(Vector2 pos, double angle, GameMap map) {
		// r * cos (a) + x = border
		// get the lowest r that is above 0 (right direction)
		double rx0 = (0 - pos.x()) / Math.cos(angle);
		double rxw = (map.getWidth() - pos.x()) / Math.cos(angle);
		double ry0 = (0 - pos.y()) / Math.sin(angle);
		double ryh = (map.getHeight() - pos.y()) / Math.sin(angle);
		double[] comparisons = new double[]{rx0, rxw, ry0, ryh};

		double r = 1e9;
		for (double rTest : comparisons) {
			if (rTest < 0) continue;
			r = Math.min(rTest, r);
		}

		return r;
	}

}


class ChargeBulletAnimation extends HarmlessAnimationBullet {
	public ChargeBulletAnimation(Tank tank, Vector2 start, Vector2 stop, long startTime, double timeLeft) {
		super(tank, start, stop, startTime, timeLeft, BULLET_SIZE_DEFAULT * .75);
	}

	final Color clr = Color.hsb(new Random().nextDouble() * 40, 0.8, 1);

	@Override
	public Color getColor() {
		return clr;
	}

	@Override
	public double animationSpeed() {
		return 1.5;
	}
}

class DeathRayBulletAnimation extends HarmlessAnimationBullet {

	public DeathRayBulletAnimation(Tank tank, Vector2 start, Vector2 stop, long startTime, double timeLeft) {
		super(tank, start, stop, startTime, timeLeft, BULLET_SIZE_RAY);
	}

	@Override
	public double animationSpeed() {
		return 10 / dist;
	}

	final Color clr = Color.GRAY;

	@Override
	public Color getColor() {
		return clr;
	}
}