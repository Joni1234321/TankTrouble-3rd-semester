package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.networking.Networking;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class ConfigurationPageController extends GenericController {

	private EventHandler<? super ActionEvent> proceedEventHandler = defaultHandler;
	private EventHandler<? super ActionEvent> returnEventHandler = defaultHandler;

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
	@FXML
	private Font x1;

	private static Mode currentMode;

	public void setProceedResponder(EventHandler<? super ActionEvent> eventHandler) {
		this.proceedEventHandler = eventHandler;
	}

	@FXML
	void onProceedClick(ActionEvent event) { // When user clicks Join/Host button
		Networking.setTentativePlayerName(nameTextField.getText());
		Networking.setTentativeHue(hueSlider.getValue());
		int port = Integer.parseInt(portTextField.getText()); // TODO: Something better here. Validation needed
		Networking.setPort(port);
		Networking.setAddress(ipTextField.getText());
		proceedEventHandler.handle(event);
	}

	public void setReturnResponder(EventHandler<? super ActionEvent> eventHandler) {
		this.returnEventHandler = eventHandler;
	}

	@FXML
	void onReturnClick(ActionEvent event) {
		returnEventHandler.handle(event);
	}

	@Override
	public void initializeController() {
		ColorAdjust colorAdjust = new ColorAdjust();
		colorAdjust.setSaturation(0.8);
		hueSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
			colorAdjust.setHue(newValue.doubleValue());
			tankImage.setEffect(colorAdjust);
		});
		// Prefill controls
		nameTextField.setText(Networking.getTentativePlayerName());
		hueSlider.setValue(Networking.getTentativeHue());
		portTextField.setText(String.valueOf(Networking.getPort()));
		ipTextField.setText(Networking.getAddress());
	}

	@Override
	ConfigurationPageController getInstance() {
		return this;
	}

	public enum Mode {
		Host,
		Join
	}

	public static Mode getCurrentMode() {
		return ConfigurationPageController.currentMode;
	}

	public void setMode(Mode mode) {
		ConfigurationPageController.currentMode = mode;
		if (mode == Mode.Host) {
			proceedButton.setText("Host");
			ipLabel.setVisible(false);
			ipTextField.setVisible(false);
		} else {
			proceedButton.setText("Join");
		}
	}

}
