package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.controller.events.KeyPressEvent;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;


public class GameController extends OnlineController {

	@FXML
	private StackPane stackPane;
	@FXML
	public AnchorPane anchorPane;

	@FXML
	void onSendClick(ActionEvent event) {
		super.onSendClick(event);
		stackPane.requestFocus();
	}

	public double drawMultiplier = 1;
	public TankTroubleAnimator tankTroubleAnimator;

	final KeyPressEvent[] keys = new KeyPressEvent[]{new KeyPressEvent(KeyCode.UP), new KeyPressEvent(KeyCode.RIGHT),
			new KeyPressEvent(KeyCode.DOWN), new KeyPressEvent(KeyCode.LEFT),
			new KeyPressEvent(KeyCode.SPACE)};


	public void fitWindowToMap() {
		GameMap map = tankTroubleAnimator.getMap();
		if (map == null) return;
		drawMultiplier = Math.min((stackPane.getHeight() - 20) / (map.getHeight()),
				(stackPane.getWidth() - 20) / (map.getWidth()));
		anchorPane.setMaxHeight((map.getHeight()) * drawMultiplier + 20);
		anchorPane.setMaxWidth((map.getWidth()) * drawMultiplier + 20);

		tankTroubleAnimator.setRedrawMap();
	}

	// Events
	@FXML
	public void onKeyPressed(KeyEvent event) {
		if (new KeyCodeCombination(KeyCode.ENTER).match(event)) {
			chatMessageTextField.requestFocus();
			return;
		}

		for (KeyPressEvent key : keys) {
			key.pressKey(event);
		}
	}

	@FXML
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
		super.initializeController();
		// Set pane

		this.gameState = GameState.inGame;

		anchorPane.setTranslateX(10);
		anchorPane.setTranslateY(10);

		stackPane.widthProperty().addListener((observableValue, number, t1) -> fitWindowToMap());
		stackPane.heightProperty().addListener((observableValue, number, t1) -> fitWindowToMap());

		tankTroubleAnimator = new TankTroubleAnimator(this);
		fitWindowToMap();
		tankTroubleAnimator.start();
	}

	public void stopAnimation() {
		tankTroubleAnimator.stop();
	}
}
