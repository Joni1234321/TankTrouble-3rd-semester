package dk.dtu.tanktrouble.app.model.gameMap;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class MapSquare implements Serializable {
    public boolean generationVisited = false;
    private final Wall wallUp;
    private final Wall wallDown;
    private final Wall wallLeft;
    private final Wall wallRight;


    public MapSquare(Wall wallUp, Wall wallDown, Wall wallLeft, Wall wallRight) {
        this.wallUp = wallUp;
        this.wallDown = wallDown;
        this.wallLeft = wallLeft;
        this.wallRight = wallRight;
    }

    public void removeWall(int direction){
        switch (direction) {
            case 0 -> wallDown.active = false;
            case 1 -> wallUp.active = false;
            case 2 -> wallRight.active = false;
            case 3 -> wallLeft.active = false;
            default -> {
            }
        }
    }

    public int getNumberOfActiveWalls(){
        return (wallDown.active ? 1 : 0) +
                (wallUp.active ? 1 : 0)+
                (wallRight.active ? 1 : 0)+
                (wallLeft.active ? 1 : 0);
    }

    public List<Wall> getAllActiveWalls(){
        List<Wall> re = new LinkedList<>();
        if(hasWallUp())     re.add(wallUp);
        if(hasWallDown())   re.add(wallDown);
        if(hasWallLeft())   re.add(wallLeft);
        if(hasWallRight())  re.add(wallRight);
        return re;
    }

    public boolean hasWallUp() {
        return wallUp.active;
    }

    public boolean hasWallDown() {
        return wallDown.active;
    }

    public boolean hasWallLeft() {
        return wallLeft.active;
    }

    public boolean hasWallRight() {
        return wallRight.active;
    }

    public Wall getWallUp() {
        return wallUp;
    }

    public Wall getWallDown() {
        return wallDown;
    }

    public Wall getWallLeft() {
        return wallLeft;
    }

    public Wall getWallRight() {
        return wallRight;
    }
}
