package dk.dtu.tanktrouble.app.model.gameMap;

import dk.dtu.tanktrouble.app.model.geometry.Polygon;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.util.List;

public class Wall implements Serializable {

    public static List<Wall> allWalls = null;

    public static final double THICKNESS = 0.1;
    public static final double SQUARE_SIZE = 1;

    public boolean active;

    final int startX;
    final int startY;
    final int endX;
    final int endY;
    Polygon polygon;

    public Wall(int startX, int startY, int endX, int endY){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        active = true;
        allWalls.add(this);
        calculatePolygon();
    }

    public Rectangle getRectangleDraw (double drawMultiplier) {
        double x1 = startX*SQUARE_SIZE*drawMultiplier;
        double y1 =  startY*SQUARE_SIZE*drawMultiplier;
        double x2 = endX*SQUARE_SIZE*drawMultiplier;
        double y2 = endY*SQUARE_SIZE*drawMultiplier;

        Rectangle r = new Rectangle();
        r.setX(x1-THICKNESS/2*drawMultiplier);
        r.setY(y1-THICKNESS/2*drawMultiplier);
        r.setWidth(x2-x1+THICKNESS*drawMultiplier);
        r.setHeight(y2-y1+THICKNESS*drawMultiplier);
        return r;
    }

    public void calculatePolygon () {
        double x1 = startX*SQUARE_SIZE;
        double y1 = startY*SQUARE_SIZE;
        double x2 = endX*SQUARE_SIZE;
        double y2 = endY*SQUARE_SIZE;

        polygon = new Polygon(new double[]{x1-THICKNESS/2, x2+THICKNESS/2, x2+THICKNESS/2, x1-THICKNESS/2},
                new double[]{y1-THICKNESS/2, y1-THICKNESS/2, y2+THICKNESS/2, y2+THICKNESS/2});
    }

    public Polygon getPolygon () {
        return polygon;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }
}
