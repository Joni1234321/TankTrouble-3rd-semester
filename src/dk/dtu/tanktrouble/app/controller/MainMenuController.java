package dk.dtu.tanktrouble.app.controller;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

public class MainMenuController extends GenericController {

	@FXML
	private ImageView imageView;

	private Runnable exitResponder = () -> {
	}; // Do nothing as default.
	private Runnable hostGameResponder = () -> {
	}; // Do nothing as default.
	private Runnable joinGameResponder = () -> {
	}; // Do nothing as default.

	public TankAnimation animation;


	public void setExitResponder(Runnable responder) {
		exitResponder = responder;
	}

	@FXML
	void onExitClick(ActionEvent event) {
		animation.stop();
		exitResponder.run();
	}

	public void setHostGameResponder(Runnable responder) {
		hostGameResponder = responder;
	}

	@FXML
	void onHostClick(ActionEvent event) {
		animation.stop();
		hostGameResponder.run();
	}

	public void setJoinGameResponder(Runnable responder) {
		joinGameResponder = responder;
	}

	@FXML
	void onJoinClick(ActionEvent event) {
		animation.stop();
		joinGameResponder.run();
	}

	@Override
	MainMenuController getInstance() {
		return this;
	}

	@Override
	public void initializeController() {
		animation = new TankAnimation();
		animation.start();
	}

	class TankAnimation extends AnimationTimer {
		long last = System.nanoTime();
		double hue = Math.random()*2-1;
		final ColorAdjust colorAdjust;

		public TankAnimation () {
			colorAdjust = new ColorAdjust();
			colorAdjust.setSaturation(0.8);
			colorAdjust.setHue(hue);
		}

		@Override
		public void handle(long now) {
			double deltaTime = (double) (now - last) / 1e9;
			imageView.setRotate(imageView.getRotate() + 5 * deltaTime);
			hue = colorAdjust.getHue();
			while (hue >= 1) {
				hue -= 2;
			}
			colorAdjust.setHue(hue + 0.05 * deltaTime);
			imageView.setEffect(colorAdjust);
			last = now;
		}
	}
}
