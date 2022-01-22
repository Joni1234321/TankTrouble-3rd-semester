package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.networking.Networking;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class ConfigurationPageController extends GenericController {

	private EventHandler<? super ActionEvent> proceedEventHandler = defaultHandler;
	private EventHandler<? super ActionEvent> returnEventHandler = defaultHandler;

	private TankImageAnimation animation;
	private TankImageAnimation.AnimationSetting animationSetting;

	@FXML
	private Slider hueSlider;
	@FXML
	private TextField ipTextField;
	@FXML
	private TextField portTextField;
	@FXML
	private TextField nameTextField;
	@FXML
	private Label ipLabel;
	@FXML
	private Button proceedButton;
	@FXML
	private ImageView tankImage;

	public void setProceedResponder(EventHandler<? super ActionEvent> eventHandler) {
		this.proceedEventHandler = eventHandler;
	}

	@FXML
	void onProceedClick(ActionEvent event) { // When user clicks Join/Host button
		animation.stop();
		int port;
		try {
			if (portTextField.getText().equals("")) {
				port = Networking.getPort();
			} else {
				port = Integer.parseInt(portTextField.getText());
				if (port < 1024 || port > 49151)
					throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			invalidPortAlert().show();
			return;
		}
		String address;
		if (!ipTextField.isVisible() || ipTextField.getText().equals("")) {
			address = Networking.standardAddress;
		} else {
			address = ipTextField.getText();
		}
		Networking.setTentativePlayerName(nameTextField.getText());
		Networking.setTentativeHue(hueSlider.getValue());
		Networking.setPort(port);
		Networking.setAddress(address);
		proceedEventHandler.handle(event);
	}

	private static Alert invalidPortAlert() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Invalid port number");
		alert.setContentText("Port number should be a number between 1024 and 49151");
		return alert;
	}

	public void setReturnResponder(EventHandler<? super ActionEvent> eventHandler) {
		this.returnEventHandler = eventHandler;
	}

	@FXML
	void onReturnClick(ActionEvent event) {
		animation.stop();
		returnEventHandler.handle(event);
	}

	@Override
	public void initializeController() {
		animation = new TankImageAnimation(tankImage);
		animation.animateHue = false; // The user should use the slider to alter hue
		animation.setImageSetting(animationSetting);
		Networking.setTentativeHue(animationSetting.getHue());
		animation.start();

		// Hue-slider listener
		hueSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
			double newHue = newValue.doubleValue();
			animation.setHue(newHue);
			Networking.setTentativeHue(newHue);
		});

		// Prefill controls
		nameTextField.setText(Networking.getTentativePlayerName());
		hueSlider.setValue(Networking.getTentativeHue());
		portTextField.setPromptText(String.valueOf(Networking.getPort()));
	}

	public void setAnimationSetting(TankImageAnimation.AnimationSetting animationSetting) {
		this.animationSetting = animationSetting; // Is only shown on initializeController
	}

	public TankImageAnimation.AnimationSetting getAnimationSetting() {
		return animation.getImageSetting();
	}

	@Override
	ConfigurationPageController getInstance() {
		return this;
	}

	public enum Mode {
		Host,
		Join
	}

	public void setMode(Mode mode) {
		if (mode == Mode.Host) {
			proceedButton.setText("Host");
			ipLabel.setVisible(false);
			ipTextField.setVisible(false);
		} else {
			if (Networking.getAddress().equals(Networking.standardAddress))
				ipTextField.setPromptText("localhost");
			else
				ipTextField.setText(Networking.getAddress());
			proceedButton.setText("Join");
		}
	}
}
