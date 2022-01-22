package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.powerups.sharedbullets.FragmentBullet;
import javafx.scene.paint.Color;

import java.util.List;

public class TankModMine extends TankMod {
	@Override
	protected void spawnBullet(Tank tank, long now) {
		new MineBullet(tank, now);
	}

	@Override
	protected int getTotalShots() {
		return 4;
	}
}

class MineBullet extends Bullet {

	private static final double DELAY_UNTIL_INVISIBLE = 1, DELAY_UNTIL_EXPLODE = 1;
	private static final int FRAGMENT_COUNT = 5;

	public MineBullet(Tank tank, long startTime) {
		// Hack to make the bullet not move
		super(tank, tank.getTurretX(), tank.getTurretY(), tank.getAngle(), startTime, 0, BULLET_TIME_DEFAULT * 10, Tank.WIDTH * .4);
	}

	boolean triggered = false;
	long triggeredTime = 0;


	@Override
	public void updatePosition(List<Polygon> colliders, double deltaTime, long now) {
		if (triggered && triggeredTime == 0)
			triggeredTime = now;
		else if (timeTillExplode(now) < 0)
			explode(now);
	}

	@Override
	protected void onTankHit(Tank hitTank, List<Tank> aliveTanks) {
		triggered = true;
	}

	void explode(long now) {
		for (int i = 0; i < FRAGMENT_COUNT; i++) {
			new MineFragment(this.tank, pos.x(), pos.y(), Math.random() * Math.PI * 2, now);
		}
		alive = false;
	}

	@Override
	public Color getColor() {
		return triggered ? Color.DARKRED : Color.DARKGREY;
	}

	// Hack to make it go invisible, and show up again when it is triggered
	@Override
	public double getTillTimeOut(long now) {
		if (triggered)
			return timeTillExplode(now);
		else
			return DELAY_UNTIL_INVISIBLE + (double) (startTime - now) / 1e9;
	}

	private double timeTillExplode(long now) {
		return triggered ? DELAY_UNTIL_EXPLODE + (double) (triggeredTime - now) / 1e9 : Double.MAX_VALUE;
	}
}

class MineFragment extends FragmentBullet {

	public MineFragment(Tank tank, double x, double y, double angle, long startTime) {
		super(tank, x, y, angle, startTime);
	}

	@Override
	protected boolean shooterInvulnerable() {
		return false;
	}
}