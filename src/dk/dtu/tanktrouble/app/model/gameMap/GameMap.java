package dk.dtu.tanktrouble.app.model.gameMap;

import dk.dtu.tanktrouble.app.model.geometry.Vector2;
import org.jspace.Tuple;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class GameMap implements Serializable {
    final int xSize;
    final int ySize;

    final MapSquare[][] squares;

    public static GameMap activeMap;

    private final List<Wall> activeWalls;

    public GameMap(int xSize, int ySize){
        this.xSize = xSize;
        this.ySize = ySize;

        Wall.allWalls = new LinkedList<>();

        squares = new MapSquare[xSize][ySize];

        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
                Wall wallUp = j == 0 ? new Wall(i,j,i+1,j) : squares[i][j-1].getWallDown();
                Wall wallLeft = i == 0 ? new Wall(i,j,i,j+1) : squares[i-1][j].getWallRight();
                Wall wallDown = new Wall(i,j+1,i+1,j+1);
                Wall wallRight = new Wall(i+1,j,i+1,j+1);

                squares[i][j] = new MapSquare(wallUp,wallDown,wallLeft,wallRight);
            }
        }
        generateRandomMap();

        activeWalls = Wall.allWalls.stream().filter(w -> w.active).collect(Collectors.toList());
        activeMap = this;
    }

    public List<Wall> getNearbyWalls(Vector2 pos){return getNearbyWalls(pos.x,pos.y);}
    public List<Wall> getNearbyWalls(double x, double y){
        return getNearbyWalls((int) x, (int) y);
    }

    public List<Wall> getNearbyWalls(int x, int y){
        int minX = x > 0 ? x-1 : 0;
        int minY = y > 0 ? y-1 : 0;
        int maxX = x < xSize-2 ? x+1 : xSize-1;
        int maxY = y < ySize-2 ? y+1 : ySize-1;

        Set<Wall> re = new LinkedHashSet<>();
        re.addAll(squares[minX][y].getAllActiveWalls());
        re.addAll(squares[maxX][y].getAllActiveWalls());
        re.addAll(squares[x][minY].getAllActiveWalls());
        re.addAll(squares[x][maxY].getAllActiveWalls());

        return re.stream().toList();
    }

    public List<Wall> getActiveWalls(){
        return activeWalls;
    }

    private void generateRandomMap(){
        int startingX = (int) (Math.random()*xSize);
        int startingY = (int) (Math.random()*ySize);

        LinkedList<Tuple> q = new LinkedList<>();
        q.add(new Tuple(startingX,startingY,5));
        while (!q.isEmpty()){
            Tuple current = q.remove((int)(Math.random()*q.size()));

            int x = (int) current.getElementAt(0);
            int y = (int) current.getElementAt(1);

            if(!squares[x][y].generationVisited){
                squares[x][y].generationVisited = true;
                squares[x][y].removeWall((int)current.getElementAt(2));

                if(x > 0)       q.add(new Tuple(x-1,y ,2));
                if(y > 0)       q.add(new Tuple(x,y-1 ,0));
                if(x < xSize-1) q.add(new Tuple(x+1,y ,3));
                if(y < ySize-1) q.add(new Tuple(x,y+1 ,1));
            }
        }

        for(int i = 0; i < (xSize*ySize)/4; i++){
            int x = (int)(Math.random()*(xSize-2)+1);
            int y = (int)(Math.random()*(ySize-2)+1);

            if(squares[x][y].getNumberOfActiveWalls() < 2) continue;

            int direction = (int)(Math.random()*3);
            squares[x][y].removeWall(direction);
        }
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public MapSquare[][] getSquares() {
        return squares;
    }
}
