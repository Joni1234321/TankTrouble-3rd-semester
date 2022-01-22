package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.Networking;
import dk.dtu.tanktrouble.data.Player;
import dk.dtu.tanktrouble.data.records.ChatRecord;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OnlineController extends GenericController {

	@FXML
	protected TextField chatMessageTextField;

	@FXML
	protected TextArea chatTextArea;

	@FXML
	protected ListView<Player> playersList;

	@FXML
	protected Label playersTitle;

	private static String savedChat;

	public static void clearSavedChat() {
		savedChat = "";
	}

	protected enum GameState {
		inLobby,
		inGame,
		inEndScreen
	}

	public GameState gameState;

	protected EventHandler<? super ActionEvent> sendEventHandler = defaultHandler;

	public static EventHandler<Event> onNewMessage;

	public static final Space chatSpace = new SequentialSpace();

	public void setSendChatMessageButtonClick(EventHandler<? super ActionEvent> eventHandler) {
		this.sendEventHandler = eventHandler;
	}

	@FXML
	void onSendClick(ActionEvent event) {
		this.sendEventHandler.handle(event);
	}

	@Override
	OnlineController getInstance() {
		return this;
	}

	@Override
	public void initializeController() {
		onNewMessage = event -> Platform.runLater(this::updateChat);
		chatTextArea.setText(savedChat);

		try {
			updatePlayerList();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void updatePlayerList() throws InterruptedException {
		Object[] resp = chatSpace.getp(new ActualField(Commands.LIST_OF_PLAYERS), new FormalField(Object.class));
		if (resp == null) return;

		List<Player> currentPlayers = (ArrayList<Player>) resp[1];

		if (gameState != GameState.inLobby) {
			currentPlayers.sort(Comparator.comparing(a -> -a.score));
		}

		if (gameState != GameState.inEndScreen)
			playersTitle.setText("Players (" + currentPlayers.size() + ")");

		ObservableList<Player> items = FXCollections.observableList(currentPlayers);
		playersList.setItems(items);

		playersList.setCellFactory(playersUpdated());
	}

	private Callback<ListView<Player>, ListCell<Player>> playersUpdated() {
		return new Callback<>() {
			@Override
			public ListCell<Player> call(ListView<Player> p) {
				return new ListCell<>() {
					@Override
					protected void updateItem(Player player, boolean b) {
						super.updateItem(player, b);
						if (player != null) {
							HBox hBox = new HBox(5);
							hBox.setMaxWidth(playersList.getWidth() - 20);
							playersList.widthProperty().addListener((observableValue, number, t1) -> hBox.setMaxWidth(playersList.getWidth() - 20));
							hBox.setAlignment(Pos.CENTER_LEFT);
							final Label lbl = new Label(player.getName());
							FontPosture isItalic = player.isAdmin ? FontPosture.ITALIC : FontPosture.REGULAR;
							FontWeight isBold = player.equals(Networking.getCurrentPlayer()) ? FontWeight.BOLD : FontWeight.THIN;

							lbl.setFont(Font.font("System", isBold, isItalic, 15));

							HBox.setHgrow(lbl, Priority.ALWAYS);
							lbl.setMaxWidth(Double.MAX_VALUE);
							lbl.setPadding(new Insets(0, 0, 1, 0));
							ColorAdjust colorAdjust = new ColorAdjust();
							colorAdjust.setHue(player.hue);

							colorAdjust.setSaturation(0.8);

							InputStream tankInputStream = getClass().getResourceAsStream("/tank.png");

							ImageView imageView = new ImageView();
							assert tankInputStream != null;
							imageView.setImage(new Image(tankInputStream));
							imageView.setEffect(colorAdjust);
							imageView.setFitHeight(30);
							imageView.setFitWidth(30);

							if (gameState == GameState.inEndScreen) {
								int finishPlace = p.getItems().indexOf(player);
								while (finishPlace > 0 && p.getItems().get(finishPlace - 1).score == player.score)
									finishPlace--;
								Label place = new Label("#" + (finishPlace + 1));
								place.setPadding(new Insets(0.0, 0.0, 0.0, 5));
								place.setFont(Font.font("System", FontWeight.BOLD, 15));
								hBox.getChildren().add(place);
							}


							hBox.getChildren().addAll(imageView, lbl);

							if (gameState != GameState.inLobby) {
								final Label scoreLabel = new Label(player.score + "");
								scoreLabel.setPadding(new Insets(0, 0, 1, 5));
								scoreLabel.setFont(Font.font("System", 14));
								hBox.getChildren().add(scoreLabel);
							}
							setGraphic(hBox);
						}
					}
				};
			}
		};
	}

	public void updateChat() {
		try {
			Object[] in = chatSpace.getp(new ActualField(Commands.CLIENT_TO_VISUALS_CHAT_UPDATE), new FormalField(ChatRecord.class));
			if (in == null) return;
			ChatRecord chatRecord = (ChatRecord) in[1];
			chatTextArea.appendText(chatRecord.toString() + "\n");
			savedChat = chatTextArea.getText();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getChatMessage() {
		String text = chatMessageTextField.getText();
		chatMessageTextField.setText("");
		return text;
	}

}
