package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.controller.events.KeyPressEvent;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.InputStream;


public class GameController extends GenericController {

	@FXML
	private StackPane stackPane;
	@FXML
	public AnchorPane anchorPane;

	public double drawMultiplier = 1;
	public TankTroubleAnimator tankTroubleAnimator;

	final KeyPressEvent[] keys = new KeyPressEvent[]{new KeyPressEvent(KeyCode.UP), new KeyPressEvent(KeyCode.RIGHT),
			new KeyPressEvent(KeyCode.DOWN), new KeyPressEvent(KeyCode.LEFT),
			new KeyPressEvent(KeyCode.SPACE)};


	public void fitWindowToMap() {
		GameMap map = tankTroubleAnimator.getMap();
		if (map == null) return;
		drawMultiplier = Math.min((stackPane.getHeight() - 20) / (map.getYSize()),
				(stackPane.getWidth() - 20) / (map.getXSize()));
		anchorPane.setMaxHeight((map.getYSize()) * drawMultiplier + 20);
		anchorPane.setMaxWidth((map.getXSize()) * drawMultiplier + 20);

		tankTroubleAnimator.setRedrawMap();
	}

	// Events
	public void onKeyPressed(KeyEvent event) {
		for (KeyPressEvent key : keys) {
			key.pressKey(event);
		}
	}
	public void onKeyReleased(KeyEvent event) {
		for (KeyPressEvent key : keys) {
			key.releaseKey(event);
		}
	}

	// Overrides
	@Override
	GameController getInstance() {
		return this;
	}
	@Override
	public void initializeController() {
		// Load animator
		InputStream tankInputStream = getClass().getResourceAsStream("/tank.png");
		assert tankInputStream != null;

		// Set pane
		anchorPane.setTranslateX(10);
		anchorPane.setTranslateY(10);

		stackPane.widthProperty().addListener((observableValue, number, t1) -> fitWindowToMap());
		stackPane.heightProperty().addListener((observableValue, number, t1) -> fitWindowToMap());

		tankTroubleAnimator = new TankTroubleAnimator(this, new Image(tankInputStream));
		fitWindowToMap();
		tankTroubleAnimator.start();
	}

	public void stopAnimation(){
		tankTroubleAnimator.stop();
	}
}
