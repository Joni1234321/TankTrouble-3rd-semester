package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.controller.sprites.BulletSprite;
import dk.dtu.tanktrouble.app.controller.sprites.TankMath;
import dk.dtu.tanktrouble.app.controller.sprites.TankSprite;
import dk.dtu.tanktrouble.app.controller.sprites.records.BulletRecord;
import dk.dtu.tanktrouble.app.controller.sprites.records.TankRecord;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.networking.client.GameClient;
import dk.dtu.tanktrouble.data.GameSnapshot;
import dk.dtu.tanktrouble.data.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.*;

public class TankTroubleAnimator extends AnimationTimer {
    // Draws tankRecords, bulletRecords and map

    private final GameController controller;
    private final Image tankImage;
    private final long timeOffset;

    private boolean redrawMap = true;

    // Visuals
    private Collection<Rectangle> walls = new ArrayList<>();
    private final Map<String, TankSprite> tankSprites = new HashMap<>();
    private final Map<Integer, BulletSprite> bulletSprites = new HashMap<>();


    // Snapshots
    public static Space pendingSnapshots = new SequentialSpace();
    GameSnapshot snapshot;
    public static GameSnapshot oldSnapshot;

    public TankTroubleAnimator (GameController controller, Image tankImage) {
        this.controller = controller;
        this.tankImage = tankImage;
        timeOffset = TankMath.getTimeOffset();
    }


    @Override
    public void handle (long nowOffset) {
        long now = nowOffset + timeOffset;

        if (redrawMap) drawMap(getMap());

        // Get a snapshot that will be drawn in the future
        while (now > snapshot.time()) {
            GameSnapshot newSnapshot = null;

            // Get snapshot
            try {
                Object[] o = pendingSnapshots.getp(new FormalField(GameSnapshot.class));
                if (o != null) {
                    newSnapshot = (GameSnapshot) o[0];
                }
            } catch (InterruptedException ignored) {}

            if (newSnapshot == null) break;

            // Set snapshot
            oldSnapshot = snapshot;
            snapshot = newSnapshot;

            // Set animation for tankRecords
            for (TankRecord record : snapshot.tankRecords()) {
                TankSprite sprite = tankSprites.get(record.id());
                if (sprite != null) sprite.updateStopRecord(record);
            }

            // Recreate bullet sprites
            for (BulletSprite bv : bulletSprites.values()) bv.destroy();
            bulletSprites.clear();

            for (BulletRecord oldBulletRecord : oldSnapshot.bulletRecords()) {
                BulletSprite sprite = new BulletSprite(controller.anchorPane, oldBulletRecord);
                bulletSprites.put(oldBulletRecord.id(), sprite);
            }
            for (BulletRecord newBulletRecord : snapshot.bulletRecords()) {
                BulletSprite sprite = bulletSprites.get(newBulletRecord.id());
                if (sprite != null) sprite.updateStopRecord(newBulletRecord);
            }
        }

        drawMovingObjects(oldSnapshot, snapshot, now);

    }

    // Draw
    private void drawMap(GameMap map) {
        redrawMap = false;
        controller.anchorPane.getChildren().removeAll(walls);

        walls = new ArrayList<>();
        for (Wall w : map.getActiveWalls())
            walls.add(w.getRectangleDraw(controller.drawMultiplier));

        controller.anchorPane.getChildren().addAll(walls);
    }
    private void drawMovingObjects (GameSnapshot oldSnapshot, GameSnapshot snapshot, long now) {
        // Linear interpolation value
        double t = (double)(now - oldSnapshot.time()) / (snapshot.time() - oldSnapshot.time());

        // Draw tankRecords
        for (TankSprite sprite: tankSprites.values())
            sprite.draw(t, controller.drawMultiplier);

        // Draw bulletRecords
        for (BulletSprite sprite : bulletSprites.values())
            sprite.draw(t, controller.drawMultiplier);
    }

    // Load
    public void loadPlayers(List<TankRecord> tankRecords) {
        pendingSnapshots = new SequentialSpace();

        oldSnapshot = new GameSnapshot(tankRecords, new ArrayList<>(), System.nanoTime());
        snapshot = new GameSnapshot(tankRecords, new ArrayList<>(), System.nanoTime());

        // Remove old
        for (TankSprite tv : tankSprites.values()) tv.destroy();
        tankSprites.clear();
        for (BulletSprite bv : bulletSprites.values()) bv.destroy();
        bulletSprites.clear();

        // Draw tankRecords and add them to list
        for (TankRecord record : tankRecords) {
            Player player = GameClient.getPlayerById(record.id());
            double hue = player == null ? Math.random()*2-1 : player.hue;
            TankSprite tankSprite = new TankSprite(tankImage, controller.anchorPane, record, hue);
            tankSprites.put(record.id(), tankSprite);
        }
    }

    public void setRedrawMap () {
        redrawMap = true;
    }

    public GameMap getMap () {return GameMap.activeMap;}
}
