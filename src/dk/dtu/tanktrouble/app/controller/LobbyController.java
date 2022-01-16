package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.Networking;
import dk.dtu.tanktrouble.data.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

public class LobbyController extends GenericController {

	@FXML
	private TextField chatMessageTextField;
	@FXML
	private TextArea chatTextArea;
	@FXML
	private Button exitButton;
	@FXML
	private TextField lobbyNameTextField;
	@FXML
	private ListView<String> playersList;
	@FXML
	private Label playersTitle;
	@FXML
	private Slider roundsSlider;
	@FXML
	private Button sendChatMessageButton;
	@FXML
	private Button startButton;
	@FXML
	private Font x1;

	private EventHandler<? super ActionEvent> returnEventHandler = defaultHandler;
	private EventHandler<? super ActionEvent> startEventHandler = defaultHandler;
	private EventHandler<? super ActionEvent> sendEventHandler = defaultHandler;

	public static EventHandler<Event> onNewMessage;

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
		onNewMessage = event -> Platform.runLater(this::updateChat);
		setEditable();
		try {
			updatePlayerList();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setEditable() {
		boolean editable = Networking.getCurrentPlayer().isAdmin;
		lobbyNameTextField.setEditable(editable);
		roundsSlider.setDisable(!editable);
	}

	public void updatePlayerList() throws InterruptedException {
		Object[] resp = chatSpace.getp(new ActualField(Commands.LIST_OF_PLAYERS), new FormalField(Object.class));
		if (resp == null) return;

		List<Player> currentPlayers = (ArrayList<Player>) resp[1];

		List<String> currentPlayerNames = new ArrayList<>();
		for (Player player : currentPlayers) currentPlayerNames.add(player.getName());

		playersTitle.setText("Players (" + currentPlayerNames.size() + ")");
		ObservableList<String> items = FXCollections.observableList(currentPlayerNames);
		playersList.setItems(items);
	}

	public void updateChat() {
		try {
			Object[] in = chatSpace.getp(new ActualField(Commands.CLIENT_TO_VISUALS),
					new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
			if (in == null) return;
			chatTextArea.setText(chatTextArea.getText() + in[1] + ": " + in[3] + "\n");
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
