package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.Networking;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.PowerUpData.PowerUpMetaData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;

public class LobbyController extends OnlineController {

	@FXML
	public ListView<PowerUpMetaData> powerUpList;
	@FXML
	private Button startButton;

	@FXML
	private Button exitButton;

	@FXML
	private Slider roundsSlider;
	@FXML
	private Label roundsText;

	private static int lastChosenPosition = 5;

	private EventHandler<? super ActionEvent> returnEventHandler = defaultHandler;
	private EventHandler<? super ActionEvent> startEventHandler = defaultHandler;
	private EventHandler<? super ActionEvent> sendEventHandler = defaultHandler;
	public static EventHandler<? super ActionEvent> updateListOfPowerUps = defaultHandler;

	public static EventHandler<Event> onSliderUpdated;

	public static final Space chatSpace = new SequentialSpace();

	public void setReturnResponder(EventHandler<? super ActionEvent> eventHandler) {
		this.returnEventHandler = eventHandler;
	}

	@FXML
	void onExitClick(ActionEvent event) {
		returnEventHandler.handle(event);
	}

	public void setStartResponder(EventHandler<? super ActionEvent> eventHandler) {
		this.startEventHandler = eventHandler;
	}

	@FXML
	void onStartClick(ActionEvent event) {
		startEventHandler.handle(event);
	}

	public void setSendChatMessageButtonClick(EventHandler<? super ActionEvent> eventHandler) {
		this.sendEventHandler = eventHandler;
	}

	@FXML
	void onSendClick(ActionEvent event) {
		this.sendEventHandler.handle(event);
	}

	@Override
	LobbyController getInstance() {
		return this;
	}

	@Override
	public void initializeController() {
		super.initializeController();
		this.gameState = GameState.inLobby;
		onSliderUpdated = event -> Platform.runLater(this::updateRoundsSliderFromServer);
		updateListOfPowerUps = event -> Platform.runLater(() -> {
			ObservableList<PowerUpMetaData> powerUps = FXCollections.observableList(PowerUpData.powerUps);
			powerUpList.setItems(powerUps);
		});
		setEditable();

		roundsSlider.valueProperty().addListener((observableValue, number, t1) -> updateRoundsText());
		roundsSlider.setOnMouseReleased(roundsChosen());
		if (startButton.isVisible()) {
			roundsSlider.setValue(lastChosenPosition);
			roundsChosen().handle(null);
		} else updateRoundsSliderFromServer();

		initiateListOfPowerUps();
	}

	private void initiateListOfPowerUps() {
		powerUpList.setBackground(Background.EMPTY);

		ObservableList<PowerUpMetaData> powerUps = FXCollections.observableList(PowerUpData.powerUps);
		powerUpList.setItems(powerUps);
		powerUpList.setCellFactory(classListView -> new powerupListViewCell());
	}

	private EventHandler<MouseEvent> roundsChosen() {
		return mouseEvent -> {
			int rounds = updateRoundsText();
			try {
				lastChosenPosition = (int) roundsSlider.getValue();
				Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER
						, Commands.NUMBER_OF_ROUNDS_UPDATED
						, new int[]{(Integer) (int) roundsSlider.getValue(), rounds});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}


	private void updateRoundsSliderFromServer() {
		try {
			Object[] resp = chatSpace.getp(new ActualField(Commands.NUMBER_OF_ROUNDS_UPDATED), new FormalField(Object.class));
			if (resp == null) return;

			int newPos = (int) resp[1];
			roundsSlider.valueProperty().setValue(newPos);
			roundsFormatted(getScaledRounds(newPos));
		} catch (Exception ignored) {
		}
	}

	private void roundsFormatted(int in) {
		if (in == (int) 1e9) {
			roundsText.setText("Rounds: Infinite");
		} else
			roundsText.setText("Rounds: " + in);
	}

	private int updateRoundsText() {
		int rounds = getScaledRounds((int) Math.round(roundsSlider.getValue()));
		roundsFormatted(rounds);
		return rounds;
	}

	private int getScaledRounds(int roundsIn) {
		int rounds;
		if (roundsIn <= 10)
			rounds = roundsIn;
		else if (roundsIn < 19) {
			rounds = 10 + (roundsIn - 10) * 5;
		} else {
			rounds = (int) 1e9;
		}
		return rounds;
	}

	private void setEditable() {
		boolean editable = Networking.getCurrentPlayer().isAdmin;
		roundsSlider.setDisable(!editable);
		startButton.setVisible(editable);
	}

	private class powerupListViewCell extends ListCell<PowerUpMetaData> {
		@Override
		protected void updateItem(PowerUpMetaData aClass, boolean b) {
			super.updateItem(aClass, b);
			if (aClass != null) {
				PowerUpDataController data = new PowerUpDataController();
				data.setInfo(aClass);
				powerUpList.widthProperty().addListener((observableValue, number, t1) -> data.getBox().setMaxWidth(powerUpList.getWidth() - 40));
				data.getBox().setMaxWidth(powerUpList.getWidth() - 40);
				setGraphic(data.getBox());
			}
		}
	}

	private static class PowerUpDataController {

		@FXML
		private VBox powerUpHolder;

		@FXML
		private Label powerUpName;

		@FXML
		private CheckBox isActiveBox;

		@FXML
		private Label powerUpDescription;

		@FXML
		private HBox nameAndStateBar;

		@FXML
		private ImageView powerUpIcon;

		public PowerUpDataController() {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/powerUpListItem.fxml"));
			fxmlLoader.setController(this);
			try {
				fxmlLoader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void setInfo(PowerUpMetaData data) {
			powerUpName.setText(data.getName());
			if (data.getDescription().equals("")) {
				powerUpDescription.setDisable(true);
				powerUpDescription.setMaxHeight(0.0);
				powerUpDescription.setVisible(false);
			} else
				powerUpDescription.setText(data.getDescription());
			isActiveBox.setSelected(data.isActive);
			isActiveBox.setDisable(!Networking.getCurrentPlayer().isAdmin);
			powerUpIcon.setImage(data.getImage());

			isActiveBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
				try {
					data.isActive = !aBoolean;
					Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER
							, Commands.POWER_UPS_UPDATED
							, new int[]{data.getId(), data.isActive ? 1 : 0});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}

		public VBox getBox() {
			return powerUpHolder;
		}
	}
}
