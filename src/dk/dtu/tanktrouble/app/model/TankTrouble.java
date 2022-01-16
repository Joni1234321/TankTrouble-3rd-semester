package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.app.controller.sprites.records.BulletRecord;
import dk.dtu.tanktrouble.app.controller.sprites.records.TankRecord;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.server.PlayerBroadcaster;
import dk.dtu.tanktrouble.data.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TankTrouble extends GameLoop {

    long msUntilGameOver;

    final PlayerBroadcaster broadcaster;

    public GameMap map;
    public List<Tank> tanks;
    final List<Bullet> bullets;

    private static final int MIN_WIDTH = 4, MAX_WIDTH = 10, MIN_HEIGHT = 4, MAX_HEIGHT = 8;
    private static final int MIN_SIZE = 30, MAX_SIZE = 72;

    public TankTrouble(PlayerBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        bullets = new ArrayList<>();
    }

    public void generateNewLevel(List<Player> players) {
        this.map = generateMap();
        this.tanks = generateTanks(map, players);
        this.bullets.clear();
        msUntilGameOver = 2000;
    }


    // Loop
    @Override
    public boolean doTick(long now, boolean sendSnapshot) {
        // Move
        for (Tank tank : tanks) {
            tank.moveKeyPress(SPT, tanks);
        }

        // Shoot if alive
        List<Tank> aliveTanks = new ArrayList<>();
        for (Tank tank : tanks) {
            if (tank.isAlive()) {
                if (tank.player.spacePressed) {
                    Bullet b = tank.shoot(now);
                    if (b != null) bullets.add(b);
                }
                aliveTanks.add(tank);
            }
            tank.player.spacePressed = false;
        }

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.updatePosition(SPT);

            Tank destroyedTank = bullet.killTankOnHit(aliveTanks);
            if (destroyedTank != null) {
                aliveTanks.remove(destroyedTank);
            }

            if (!bullet.isAlive(now)) {
                bullets.remove(i);
                bullet.tank.bullets.remove(bullet);
            }
        }

        // Send snapshot
        if (sendSnapshot) {
            try {broadcaster.sendToPlayers(Commands.NEW_SNAPSHOT, Commands.generateSnapshot(TICK_DELAY, TPS, now, TankRecord.generateRecords(tanks), BulletRecord.generateRecords(bullets, now))); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        // Game Over Condition
        if (aliveTanks.size() <= 1 && aliveTanks.size() < tanks.size())
            msUntilGameOver -= MSPT;

        return msUntilGameOver > 0;
    }

    // Generation
    private GameMap generateMap()  {
        // Generate map
        Random rnd = new Random();
        int width, height;

        do {
            width = rnd.nextInt(MAX_WIDTH-MIN_WIDTH+1)+MIN_WIDTH;
            height = rnd.nextInt(MAX_HEIGHT-MIN_HEIGHT+1)+MIN_HEIGHT;
        } while (width*height < MIN_SIZE && width*height > MAX_SIZE && width < height);

        return new GameMap(width, height);
    }
    private List<Tank> generateTanks (GameMap map, List<Player> players) {
        Random rnd = new Random();
        List<Tank> tanks = new ArrayList<>();

        for (Player player : players) {
            Tank tank;
            do {
                tank = new Tank(
                        map,
                        rnd.nextInt(map.getXSize()) + 0.5,
                        rnd.nextInt(map.getYSize()) + 0.5,
                        Math.random() * Math.PI * 2, player.getId());

                tank.player = player;
                tank.resurrect();

            } while (tank.isCollidingWithTank(tanks));

            tanks.add(tank);
        }

        return tanks;
    }

}
