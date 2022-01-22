package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;

import java.util.List;

public class PowerUp {

	public static final double SIZE = .25;

	public boolean alive = true;
	public Tank tank;
	public double x, y, angle;
	public final TankMod tankMod;
	public final String id;
	static int currId = 0;

	public PowerUp(double x, double y, double angle, TankMod tankMod) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.tankMod = tankMod;

		id = "Powerup: " + (++currId);
	}

	public Tank hitTank(List<Tank> tanks) {
		// check if hit a tank
		for (Tank tank : tanks) {
			if (tank.canPickupMod() && tank.getPolygon().intersect(getPolygon())) {
				alive = false;
				return tank;
			}
		}
		// if not hitting any tank
		return null;
	}

	public Polygon getPolygon() {
		return new Polygon(spritePts).moveAndRotate(new Vector2(x, y), angle);
	}

	private static final Vector2[] spritePts = new Vector2[]{
			new Vector2(SIZE / 2, SIZE / 2),
			new Vector2(SIZE / 2, -SIZE / 2),
			new Vector2(-SIZE / 2, -SIZE / 2),
			new Vector2(-SIZE / 2, SIZE / 2)};

}
