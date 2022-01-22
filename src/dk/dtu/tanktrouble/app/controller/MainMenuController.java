package dk.dtu.tanktrouble.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

	private TankImageAnimation animation;
	private TankImageAnimation.AnimationSetting animationSetting;

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
		animation = new TankImageAnimation(imageView);
		animation.setImageSetting(animationSetting);
		animation.start();
	}

	public void setAnimationSetting(TankImageAnimation.AnimationSetting animationSetting) {
		this.animationSetting = animationSetting; // Is only shown on initializeController
	}

	public TankImageAnimation.AnimationSetting getAnimationSetting() {
		return animation.getImageSetting();
	}

}
