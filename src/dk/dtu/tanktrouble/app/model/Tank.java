package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import dk.dtu.tanktrouble.app.model.powerups.TankMod;
import dk.dtu.tanktrouble.app.model.powerups.TankModDefault;
import dk.dtu.tanktrouble.data.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Tank {

	private static final double PI2 = Math.PI * 2;
	public static final double MOVE_SPEED = 1, ANGLE_SPEED = PI2 * 0.75;
	public static final int DEFAULT_MAX_BULLETS = 5;
	public static final int MOD_QUEUE_MAX = 3;

	public static final double WIDTH = 0.40;
	public static final double HEIGHT = 0.40;

	private boolean canGetHit, isAlive;
	public int maxBullets;

	public final GameMap map;

	private TankMod tankMod;
	private Queue<TankMod> modQueue;

	public final List<Bullet> bullets = new ArrayList<>();
	public int bulletCount;

	private double x, y, angle;

	private final String id;
	public Player player;

	Tank killer;

	public Tank(GameMap map, double x, double y, double angle, String id) {
		this.map = map;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.id = id;
		resurrect();
	}

	//region shooting
	public void shoot(long now) {
		tankMod.shoot(this, now);
	}

	public double getTurretX() {
		return x + Math.cos(angle) * getTurretOffset();
	}

	public double getTurretY() {
		return y + Math.sin(angle) * getTurretOffset();
	}

	double getTurretOffset() {
		return WIDTH / 2 - 0.1;
	}
	//endregion

	//region movement
	public Polygon getPolygon() {
		return getPolygon(x, y, angle);
	}

	public Polygon getPolygon(double x, double y, double angle) {
		return new Polygon(spritePts).moveAndRotate(new Vector2(x, y), angle);
	}


	public boolean isCollidingWithWall(double x, double y, double angle) {
		for (Wall w : map.getNearbyWalls(x, y))
			if (getPolygon(x, y, angle).intersect(w.getPolygon())) return true;

		return false;
	}

	public boolean isCollidingWithTank(List<Tank> tanks) {
		return isCollidingWithTank(x, y, angle, tanks);
	}

	public boolean isCollidingWithTank(double x, double y, double angle, List<Tank> tanks) {
		for (Tank other : tanks) {
			if (other == this || !other.isAlive()) continue;
			if (getPolygon(x, y, angle).intersect(other.getPolygon())) return true;
		}
		return false;
	}

	public void moveKeyPress(List<Tank> tanks, double deltaTime, long now) {
		tankMod.move(this, tanks, deltaTime, now);
	}

	public void tryMoveTo(double x, double y, double angle, List<Tank> tanks) {
		if (isValidTransform(x, y, angle, tanks)) {
			this.x = x;
			this.y = y;
			this.angle = angle;
		}
	}

	protected boolean isValidTransform(double x, double y, double angle, List<Tank> tanks) {
		return !(isCollidingWithWall(x, y, angle) || (isAlive() && isCollidingWithTank(x, y, angle, tanks)));
	}

	//endregion


	// THESE TWO WILL HAVE TO INCLUDE THE SAME VARIABLES
	public void death(Tank killer) {
		maxBullets = 0;
		canGetHit = false;
		isAlive = false;
		this.killer = killer;
	}

	public void resurrect() {
		tankMod = new TankModDefault();
		modQueue = new LinkedList<>();
		bulletCount = 0;
		maxBullets = DEFAULT_MAX_BULLETS;
		canGetHit = true;
		isAlive = true;
		killer = null;
	}


	public void addMod(TankMod tankMod) {
		if (this.tankMod.getClass() == TankModDefault.class)
			this.tankMod = tankMod;
		else
			modQueue.add(tankMod);
	}

	public void nextMod() {
		if (modQueue.isEmpty())
			tankMod = new TankModDefault();
		else
			tankMod = modQueue.poll();
	}


	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getAngle() {
		return angle;
	}

	public boolean canGetHit() {
		return canGetHit;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public String getId() {
		return id;
	}

	public boolean canPickupMod() {
		return canGetHit && modQueue.size() < MOD_QUEUE_MAX;
	}

	// All the points on the tank
	private static final Vector2[] spritePts = new Vector2[]{
			// BODY
			new Vector2(WIDTH / 2 * (240. / 296), HEIGHT / 2 * (195. / 296)),

			// TURRET
			new Vector2(WIDTH / 2 * (240. / 296), HEIGHT / 2 * (42. / 296)),
			new Vector2(WIDTH / 2, HEIGHT / 2 * (42. / 296)),
			new Vector2(WIDTH / 2, -HEIGHT / 2 * (42. / 296)),
			new Vector2(WIDTH / 2 * (240. / 296), -HEIGHT / 2 * (42. / 296)),

			// BODY CONTINUED
			new Vector2(WIDTH / 2 * (240. / 296), -HEIGHT / 2 * (195. / 296)),
			new Vector2(-WIDTH / 2 * (240. / 296), -HEIGHT / 2 * (195. / 296)),
			new Vector2(-WIDTH / 2 * (240. / 296), HEIGHT / 2 * (195. / 296))
	};


}
