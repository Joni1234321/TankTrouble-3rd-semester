package dk.dtu.tanktrouble.app.controller;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.Networking;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EndScreenController extends OnlineController {

	@FXML
	private Button exitToMain;
	@FXML
	private Button goToLobby;

	@Override
	EndScreenController getInstance() {
		return this;
	}

	@Override
	public void initializeController() {
		this.gameState = GameState.inEndScreen;
		super.initializeController();

		exitToMain.setOnAction(actionEvent -> {
			try {
				if (Networking.getCurrentPlayer().isAdmin)
					Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER,
							Commands.CLOSE_SERVER, new Object());
				else
					Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER,
							Commands.CLIENT_TO_SERVER_QUIT, new Object());
			} catch (InterruptedException ignore) {
			}
		});
		if (!Networking.getCurrentPlayer().isAdmin)
			goToLobby.setDisable(true);
		else
			goToLobby.setOnAction(actionEvent -> {
				try {
					Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER,
							Commands.RETURN_TO_LOBBY, new Object());
				} catch (InterruptedException ignored) {
				}
			});
	}
}
