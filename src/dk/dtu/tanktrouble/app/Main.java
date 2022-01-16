package dk.dtu.tanktrouble.app;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.controller.*;
import dk.dtu.tanktrouble.app.networking.Networking;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

	public static final int DEFAULT_WIDTH = 820;
	public static final int DEFAULT_HEIGHT = 620;
	public static Stage stage;

	public static EventHandler<Event> onMapReceived;
	public static EventHandler<Event> onPlayersUpdated;

	private static boolean isRunning = true;
	public static EventHandler<Event> toMainMenuEvent;

	private static GenericController controller;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;

		stage.setTitle("Tank Trouble");
		stage.setMinHeight(400);
		stage.setMinWidth(400);
		stage.getIcons().add(new Image("/tank.png"));
		setUpEvents();

		stage.setOnCloseRequest(this::onCloseRequest);

		displayMainMenu();
	}

	public interface SceneInitializer {
		void initializeScene(Object controllerInstance, Scene scene);
	}

	private void display(String fxmlResource, GenericController.ControlResponder controllerSpecificActions) {
		display(fxmlResource, controllerSpecificActions, (controller, scene) -> { });
	}
	private void display(String fxmlResource, GenericController.ControlResponder controllerSpecificActions, SceneInitializer sceneInitializer) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		Parent view = null;
		try {
			view = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert view != null;
		Scene scene = new Scene(view, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		stage.setScene(scene);

		stage.show();
		controller = loader.getController();
		controller.initializeScene(sceneInitializer, scene);
		controller.setOnControlResponder(controllerSpecificActions);
		controller.beginControl();
	}


	private void setUpEvents(){
		onMapReceived = event -> Platform.runLater(this::displayTankTroubleLevel);
		toMainMenuEvent = event -> Platform.runLater(()->{if (isRunning) displayMainMenu();});
	}

	private void displayMainMenu() {
		display("/mainMenu.fxml", object -> {
			MainMenuController mainMenuController = (MainMenuController) object;
			mainMenuController.setExitResponder(stage::close);
			mainMenuController.setHostGameResponder(() -> displayConfigurationPage(ConfigurationPageController.Mode.Host));
			mainMenuController.setJoinGameResponder(() -> displayConfigurationPage(ConfigurationPageController.Mode.Join));
		});
	}
	private void displayLobby() {
		display("/lobby.fxml", object -> {
			LobbyController lobbyController = (LobbyController) object;
			Main.onPlayersUpdated = event -> Platform.runLater(() -> {
				try {
					lobbyController.updatePlayerList();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			lobbyController.setStartResponder((e) -> Networking.startGame());
			lobbyController.setReturnResponder((e) -> {
				Networking.quitLobby();
				displayMainMenu();
			});
			lobbyController.setSendChatMessageButtonClick((e) -> Networking.sendChatMessage(lobbyController.getChatMessage()));
		});
	}
	private void displayTankTroubleLevel() {
		if (controller.getClass() != GameController.class) {
			display("/gameBoard.fxml", (o) -> {
			}, (object, scene) -> {
				GameController gameController = (GameController) object;
				scene.setOnKeyPressed(gameController::onKeyPressed);
				scene.setOnKeyReleased(gameController::onKeyReleased);
			});
		}
		((GameController) controller).fitWindowToMap();
		((GameController) controller).tankTroubleAnimator.loadPlayers(TankTroubleAnimator.oldSnapshot.tankRecords());
	}

	private void displayConfigurationPage(ConfigurationPageController.Mode mode) {
		display("/configurationPage.fxml", (object) -> {
			ConfigurationPageController configController = (ConfigurationPageController) object;
			configController.setMode(mode);
			configController.setReturnResponder((e) -> displayMainMenu());

			if (mode == ConfigurationPageController.Mode.Host) {
				configController.setProceedResponder(actionEvent -> {
					Networking.startServer();
					if (Networking.startClient() != null) displayLobby();
					else System.out.println("Could not connect to server");
				});
			} else {
				configController.setProceedResponder(actionEvent -> {
					if (Networking.startClient() != null) displayLobby();
					else System.out.println("Could not connect to server"); //TODO display this in the UI
				});
			}
		}, (o, scene) -> {
		});
	}


	//region events
	void onCloseRequest (Event ignored) {
		isRunning = false;
		try {
			((GameController)controller).stopAnimation();
		}catch (Exception ignored1){}
		try {
			if(Networking.getCurrentPlayer() != null) {
				if (Networking.getCurrentPlayer().isAdmin)
					Networking.getCurrentPlayer().channel.put(Commands.TO_PLAYER_HANDLER, Commands.CLOSE_SERVER, new Object());
				else {
					Networking.getCurrentPlayer().channel.put(Commands.TO_PLAYER_HANDLER,Commands.CLIENT_TO_SERVER_QUIT, new Object());
				}
			}
			else {
				Networking.quitLobby();
			}
		}
		catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	//endregion
}

