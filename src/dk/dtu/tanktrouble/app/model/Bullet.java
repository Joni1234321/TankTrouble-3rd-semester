package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;

import java.util.List;

public class Bullet {

    public static final double BULLET_SPEED = Tank.MOVE_SPEED * 1.5;
    static final long BULLET_TIME_S = 10 * 1000000000L;

    private boolean alive = true;
    private boolean isHarmless = true;

    private final GameMap map;
    public final Tank tank;
    Vector2 pos, dir;
    final long startTime;
    final int id;
    static int currId = 0;

    public Bullet(GameMap map, Tank tank, double x, double y, double angle, long startTime) {
        this.map = map;
        this.tank = tank;

        pos = new Vector2(x, y);
        dir = new Vector2(Math.cos(angle), Math.sin(angle));
        this.startTime = startTime;
        id = ++currId;
    }

    public void updatePosition (double deltaTime) {
        Vector2 r = dir.multiply(BULLET_SPEED*deltaTime);

        double bestT = 2, t;
        Vector2 r2 = null;
        for (Wall w : map.getNearbyWalls(pos)) {
            Object[] newMove = w.getPolygon().reflect(pos, r);
            if (newMove != null) {
                t = (Double) newMove[0];
                if (t < bestT) {
                    bestT = t;
                    r2 = (Vector2) newMove[1];
                    isHarmless = false;
                }
            }
        }

        if (r2 != null) { // Has hit wall
            pos = pos.add(r.multiply(bestT));
            dir = r2;
        } else {
            pos = pos.add(r);
        }

    }

    public Tank killTankOnHit(List<Tank> tanks) {
        // if still inside the shooting tank
        if (isHarmless && this.tank.getPolygon().isInside(pos)) {
            return null;
        }

        // check if hit a tank
        for (Tank tank : tanks) {
            if (tank.canGetHit() && tank.getPolygon().isInside(pos)) {
                alive = false;
                tank.death();
                return tank;
            }
        }

        // if not hitting any tank
        isHarmless = false;
        return null;
    }

    public double getTillTimeOut(long now) {
        return (double) (BULLET_TIME_S + startTime - now) / 1e9;
    }

    public boolean isAlive (long now) {
        return (startTime + BULLET_TIME_S > now) && alive;
    }

    public double getX() {
        return pos.x;
    }

    public double getY() {
        return pos.y;
    }

    public int getId() {
        return id;
    }
}
