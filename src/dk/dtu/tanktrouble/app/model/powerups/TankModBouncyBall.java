package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import javafx.scene.paint.Color;

import java.util.List;

public class TankModBouncyBall extends TankMod {
	@Override
	protected void spawnBullet(Tank tank, long now) {
		new BouncyBallBullet(tank, now);
	}

	@Override
	protected int getTotalShots() {
		return 2;
	}
}

class BouncyBallBullet extends Bullet {
	public BouncyBallBullet(Tank tank, long starTime) {
		super(tank, tank.getTurretX(), tank.getTurretY(), tank.getAngle(), starTime, BULLET_SPEED_DEFAULT * 2, BULLET_TIME_DEFAULT * 100, BULLET_SIZE_DEFAULT * 2);
	}

	@Override
	protected void onTankHit(Tank hitTank, List<Tank> aliveTanks) {
		// Calculate new position for tank and for bullets
		double force = 0.2;
		Vector2 vel = dir.normalize().multiply(force);
		Object[] newMove = hitTank.getPolygon().reflect(pos, dir.multiply(1e9));

		// Then move and reflect bullet
		hitTank.tryMoveTo(hitTank.getX() + vel.x(), hitTank.getY() + vel.y(), hitTank.getAngle(), aliveTanks);
		// Reflect bullet
		if (newMove != null)
			dir = (Vector2) newMove[1];
	}

	@Override
	public Color getColor() {
		return Color.BLUEVIOLET;
	}
}

