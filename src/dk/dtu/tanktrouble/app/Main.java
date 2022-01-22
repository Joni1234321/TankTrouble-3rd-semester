package dk.dtu.tanktrouble.app;

import dk.dtu.tanktrouble.app.controller.*;
import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.app.networking.Networking;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

	public static final int DEFAULT_WIDTH = 820;
	public static final int DEFAULT_HEIGHT = 620;
	public static Stage stage;

	public static EventHandler<Event> onMapReceived;
	public static EventHandler<Event> onPlayersUpdated;
	public static EventHandler<Event> onEndGame;
	public static EventHandler<Event> returnToLobby;

	private static boolean isRunning = true;
	public static EventHandler<Event> toMainMenuEvent;

	private TankImageAnimation.AnimationSetting animationSetting = new TankImageAnimation.AnimationSetting();

	public static GenericController controller;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;

		stage.setTitle("Tank Trouble");
		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.getIcons().add(new Image("/tank.png"));
		setUpEvents();

		stage.setOnCloseRequest(this::onCloseRequest);

		displayMainMenu();
	}

	public interface SceneInitializer {
		void initializeScene(Object controllerInstance, Scene scene);
	}

	private void display(String fxmlResource, GenericController.ControlResponder controllerSpecificActions) {
		display(fxmlResource, controllerSpecificActions, (controller, scene) -> {
		});
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
		Scene scene;
		if (stage.getScene() == null) {
			scene = new Scene(view, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		} else
			scene = new Scene(view, stage.getScene().getWidth(), stage.getScene().getHeight());
		stage.setScene(scene);

		stage.show();
		controller = loader.getController();
		controller.initializeScene(sceneInitializer, scene);
		controller.setOnControlResponder(controllerSpecificActions);
		controller.beginControl();
	}


	private void setUpEvents() {
		onEndGame = event -> {
			try {
				((GameController) controller).stopAnimation();
			} catch (Exception ignored) {
			}
			Platform.runLater(this::displayEndScreen);
		};
		onMapReceived = event -> Platform.runLater(this::displayTankTroubleLevel);
		toMainMenuEvent = event -> Platform.runLater(() -> {
			if (isRunning) {
				try {
					((GameController) controller).stopAnimation();
				} catch (Exception ignored) {
				}
				displayMainMenu();
			}
		});
		returnToLobby = event -> Platform.runLater(() -> displayLobby(new TankImageAnimation.AnimationSetting()));
	}

	private void displayEndScreen() {
		display("/endScreen.fxml", object -> {
			EndScreenController endScreenController = (EndScreenController) object;
			Main.onPlayersUpdated = event -> Platform.runLater(() -> {
				try {
					endScreenController.updatePlayerList();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			endScreenController.setSendChatMessageButtonClick((e) -> Networking.sendChatMessage(endScreenController.getChatMessage()));
		});
	}

	private void displayMainMenu(TankImageAnimation.AnimationSetting animationSetting) {
		this.animationSetting = animationSetting;
		displayMainMenu();
	}

	private void displayMainMenu() {
		OnlineController.clearSavedChat();
		display("/mainMenu.fxml", object -> {
			MainMenuController mainMenuController = (MainMenuController) object;
			mainMenuController.setAnimationSetting(animationSetting);
			mainMenuController.setExitResponder(stage::close);
			mainMenuController.setHostGameResponder(() -> displayConfigurationPage(ConfigurationPageController.Mode.Host, mainMenuController.getAnimationSetting()));
			mainMenuController.setJoinGameResponder(() -> displayConfigurationPage(ConfigurationPageController.Mode.Join, mainMenuController.getAnimationSetting()));
		});
	}

	private void displayLobby(TankImageAnimation.AnimationSetting animationSetting) {
		this.animationSetting = animationSetting;
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
			lobbyController.setReturnResponder((e) -> Networking.quitLobby());
			lobbyController.setSendChatMessageButtonClick((e) -> Networking.sendChatMessage(lobbyController.getChatMessage()));
		});
	}

	private void displayTankTroubleLevel() {
		if (controller.getClass() != GameController.class) {
			display("/gameBoard.fxml", (o) -> {
			}, (object, scene) -> {
				GameController gameController = (GameController) object;
				gameController.setSendChatMessageButtonClick((e) -> Networking.sendChatMessage(gameController.getChatMessage()));
				Main.onPlayersUpdated = event -> Platform.runLater(() -> {
					try {
						gameController.updatePlayerList();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});
			});
		}
		((GameController) controller).fitWindowToMap();
		((GameController) controller).tankTroubleAnimator.loadPlayers(TankTroubleAnimator.oldSnapshot.tankRecords());
	}

	private void displayConfigurationPage(ConfigurationPageController.Mode mode, TankImageAnimation.AnimationSetting animationSetting) {
		display("/configurationPage.fxml", (object) -> {
			ConfigurationPageController configController = (ConfigurationPageController) object;
			configController.setMode(mode);
			configController.setReturnResponder((e) -> displayMainMenu(configController.getAnimationSetting()));
			configController.setAnimationSetting(animationSetting);
			if (mode == ConfigurationPageController.Mode.Host) {
				configController.setProceedResponder(actionEvent -> {
					try {
						Networking.startServer();
					} catch (RuntimeException e) {
						if (couldNotStartServerAlert()) return;
					}
					if (Networking.startClient() != null) displayLobby(configController.getAnimationSetting());
					else {
						System.out.println("Could not connect to server");
						couldNotConnectAlert().showAndWait();
					}
				});
			} else {
				configController.setProceedResponder(actionEvent -> {
					if (Networking.startClient() != null) displayLobby(configController.getAnimationSetting());
					else {
						System.out.println("Could not connect to server");
						couldNotConnectAlert().showAndWait();
					}
				});
			}
		}, (o, scene) -> {
		});
	}

	private static boolean couldNotStartServerAlert() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Unable to start server du to busy port");
		alert.setContentText("There is already an open server on the given port. \n" +
				"Do you want to try to connect to this server?");
		Optional<ButtonType> result = alert.showAndWait();
		return !(result.isPresent() && result.get() == ButtonType.OK);
	}

	private static Alert couldNotConnectAlert() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Unable to connect to server");
		alert.setContentText("No open server found on the given ip.\n" +
				"Maybe the server is already in a game");
		return alert;
	}


	//region events
	void onCloseRequest(Event ignored) {
		isRunning = false;
		try {
			((GameController) controller).stopAnimation();
		} catch (Exception ignored1) {
		}
		try {
			if (Networking.getCurrentPlayer() != null) {
				if (Networking.getCurrentPlayer().isAdmin)
					Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER, Commands.CLOSE_SERVER, new Object());
				else {
					Networking.getPlayerChannel().put(Commands.TO_PLAYER_HANDLER, Commands.CLIENT_TO_SERVER_QUIT, new Object());
				}
			} else {
				Networking.quitLobby();
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	//endregion
}

