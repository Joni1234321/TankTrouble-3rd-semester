package dk.dtu.tanktrouble.app.model.powerups.sharedbullets;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import javafx.scene.paint.Color;

public class FragmentBullet extends Bullet {

	public FragmentBullet(Tank tank, double x, double y, double angle, long startTime) {
		super(tank, x, y, angle, startTime, BULLET_SPEED_DEFAULT * 2.5, BULLET_TIME_DEFAULT / 6, BULLET_SIZE_DEFAULT / 2.5);
	}

	@Override
	public void onPositionCollision(Vector2 oldDirection, Vector2 newDirection, double t) {
		alive = false;
	}

	@Override
	public Color getColor() {
		return Color.HOTPINK;
	}
}
