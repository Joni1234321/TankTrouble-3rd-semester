package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.controller.sprites.BulletSprite;
import dk.dtu.tanktrouble.app.controller.sprites.PowerUpSprite;
import dk.dtu.tanktrouble.app.controller.sprites.Sprite;
import dk.dtu.tanktrouble.app.controller.sprites.TankSprite;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.networking.client.GameClient;
import dk.dtu.tanktrouble.data.GameSnapshot;
import dk.dtu.tanktrouble.data.records.SpriteRecord;
import dk.dtu.tanktrouble.data.records.TankRecord;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.InputStream;
import java.util.*;

public class TankTroubleAnimator extends AnimationTimer {
	// Draws tankRecords, bulletRecords and map

	private final GameController controller;

	private boolean redrawMap = true;

	// Visuals
	private Collection<Rectangle> walls = new ArrayList<>();
	private final Map<String, TankSprite> tankSprites = new HashMap<>();
	private final Map<String, BulletSprite> bulletSprites = new HashMap<>();
	private final Map<String, PowerUpSprite> powerUpSprites = new HashMap<>();


	// Snapshots
	public static Space pendingSnapshots = new SequentialSpace();
	GameSnapshot snapshot;
	public static GameSnapshot oldSnapshot;

	public TankTroubleAnimator(GameController controller) {
		this.controller = controller;
	}

	@Override
	public void handle(long nowOffset) {
		long now = nowOffset + GameClient.timeOffset;

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
			} catch (InterruptedException ignored) {
			}

			if (newSnapshot == null) break;

			// Set snapshot
			oldSnapshot = snapshot;
			snapshot = newSnapshot;

			updateSprites(snapshot.tankRecords(), tankSprites, TankSprite.class);
			updateSprites(snapshot.bulletRecords(), bulletSprites, BulletSprite.class);
			updateSprites(snapshot.powerUpRecords(), powerUpSprites, PowerUpSprite.class);
		}

		double t = (double) (now - oldSnapshot.time()) / (snapshot.time() - oldSnapshot.time());
		drawSprites(tankSprites.values(), t);
		drawSprites(bulletSprites.values(), t);
		drawSprites(powerUpSprites.values(), t);
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

	private void drawSprites(Collection<? extends Sprite<?>> sprites, double t) {
		for (Sprite<?> sprite : sprites)
			sprite.draw(t, controller.drawMultiplier);
	}

	public <R extends SpriteRecord, S extends Sprite<R>> void updateSprites(List<R> records, Map<String, S> sprites, Class<S> spriteClass) {
		Map<String, S> activeSprites = new HashMap<>();

		for (R record : records) {
			S sprite = sprites.get(record.id());

			if (sprite != null) {
				// Update all sprites that do have a record
				sprite.updateStopRecord(record);
				sprites.remove(record.id());
			} else {
				// Create new sprite for every record that doesn't have one and add to dictionary
				try {
					sprite = spriteClass.getConstructor(AnchorPane.class, record.getClass()).newInstance(controller.anchorPane, record);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}

			activeSprites.put(record.id(), sprite);
		}

		// Remove all other sprites
		for (S spriteWithoutRecord : sprites.values()) {
			spriteWithoutRecord.destroy();
		}

		sprites.clear();
		sprites.putAll(activeSprites);
	}

	// Load
	public void loadPlayers(List<TankRecord> tankRecords) {
		pendingSnapshots = new SequentialSpace();

		oldSnapshot = new GameSnapshot(tankRecords, System.nanoTime() + GameClient.timeOffset);
		snapshot = new GameSnapshot(tankRecords, System.nanoTime() + GameClient.timeOffset);

		// Remove old
		for (TankSprite tv : tankSprites.values()) tv.destroy();
		tankSprites.clear();
		for (BulletSprite bv : bulletSprites.values()) bv.destroy();
		bulletSprites.clear();


		// Draw tankRecords and add them to list
		for (TankRecord record : tankRecords)
			tankSprites.put(record.id(), new TankSprite(controller.anchorPane, record));
	}

	public void setRedrawMap() {
		redrawMap = true;
	}

	public GameMap getMap() {
		return GameMap.activeMap;
	}

	// JAVAFX
	public static Image loadImage(String path) {
		InputStream tankInputStream = TankTroubleAnimator.class.getResourceAsStream(path);
		assert tankInputStream != null;
		return new Image(tankInputStream);
	}
}
