package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import dk.dtu.tanktrouble.data.Player;

import java.util.*;

public class Tank {

    private static final double PI2 = Math.PI * 2;
    public static final double MOVE_SPEED = 1, ANGLE_SPEED = PI2;
    public static final int DEFAULT_MAX_BULLETS = 5;

    public static final double WIDTH = 0.40;
    public static final double HEIGHT = 0.40;

    private boolean canGetHit, isAlive;
    private int maxBullets;

    private final GameMap map;
    private double x, y, angle;
    private final String id;
    public Player player;

    public Tank(GameMap map, double x, double y, double angle, String id) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.id = id;
        resurrect();
    }

    //region shooting
    public final List<Bullet> bullets = new ArrayList<>();
    public Bullet shoot (long now) {
        if (bullets.size() >= maxBullets) return null;
        // Spawn bullet in front of tank
        Bullet b = new Bullet(map, this, getTurretX(), getTurretY(), angle, now);
        bullets.add(b);
        return b;
    }
    public double getTurretX () {
        return x + Math.cos(angle) * getTurretOffset();
    }
    public double getTurretY () {
        return y + Math.sin(angle) * getTurretOffset();
    }
    double getTurretOffset (){
        return WIDTH / 2 - 0.1;
    }
    //endregion

    //region movement
    public Polygon getPolygon () {
        Vector2[] pts = new Vector2[]{
                new Vector2(WIDTH/2*(240./296), HEIGHT/2*(195./296)),

                new Vector2(WIDTH/2*(240./296), HEIGHT/2*(42./296)),
                new Vector2(WIDTH/2, HEIGHT/2*(42./296)),
                new Vector2(WIDTH/2, -HEIGHT/2*(42./296)),
                new Vector2(WIDTH/2*(240./296), -HEIGHT/2*(42./296)),

                new Vector2(WIDTH/2*(240./296), -HEIGHT/2*(195./296)),
                new Vector2(-WIDTH/2*(240./296), -HEIGHT/2*(195./296)),
                new Vector2(-WIDTH/2*(240./296), HEIGHT/2*(195./296))};

        // Rotate points around center and add (x,y) offset
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new Vector2(x + pts[i].x*Math.cos(angle) - pts[i].y*Math.sin(angle),
                                 y + pts[i].x*Math.sin(angle) + pts[i].y*Math.cos(angle));
        }
        return new Polygon(pts);
    }

    public boolean isCollidingWithWall () {
        for (Wall w : map.getNearbyWalls(x,y)) {
            if (getPolygon().intersect(w.getPolygon())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCollidingWithTank (List<Tank> tanks) {
        for (Tank other : tanks) {
            if (other == this || !other.isAlive()) continue;
            if (getPolygon().intersect(other.getPolygon())) {
                return true;
            }
        }
        return false;
    }

    public void moveKeyPress (double deltaTime, List<Tank> tanks) {
        moveKeyPress(deltaTime, player.upPressed, player.rightPressed, player.downPressed, player.leftPressed, tanks);
    }

    public void moveKeyPress (double deltaTime, boolean up, boolean right, boolean down, boolean left, List<Tank> tanks) {
        double distX = Math.cos(angle) * MOVE_SPEED * deltaTime;
        double distY = Math.sin(angle) * MOVE_SPEED * deltaTime;

        int direction = (up ? 1 : 0) + (down ? -1 : 0);

        double oldX = x, oldY = y, oldAngle = angle;

        x += distX * direction;
        y += distY * direction;

        direction = (right ? 1 : 0) + (left ? -1 : 0);

        setAngle(angle + direction * ANGLE_SPEED * deltaTime);

        if (isCollidingWithWall() || (isAlive() && isCollidingWithTank(tanks))) {
            x = oldX;
            y = oldY;
            angle = oldAngle;
        }
    }
    //endregion


    // THESE TWO WILL HAVE TO INCLUDE THE SAME VARIABLES
    public void death () {
        System.out.println("Tank was hit!");
        maxBullets = 0;
        canGetHit = false;
        isAlive = false;

    }
    public void resurrect() {
        maxBullets = DEFAULT_MAX_BULLETS;
        canGetHit = true;
        isAlive = true;
    }

     void setAngle(double newAngle) {
        angle = (((newAngle % PI2) + PI2) % PI2);  // positive modulo: https://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
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

    public String getId () {
        return id;
    }



}
