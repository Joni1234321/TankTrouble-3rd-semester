package dk.dtu.tanktrouble.app.networking.client;

import dk.dtu.tanktrouble.app.Main;
import dk.dtu.tanktrouble.app.controller.LobbyController;
import dk.dtu.tanktrouble.app.controller.OnlineController;
import dk.dtu.tanktrouble.app.controller.TankTroubleAnimator;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.networking.Networking;
import dk.dtu.tanktrouble.data.GameSnapshot;
import dk.dtu.tanktrouble.data.Player;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.records.ChatRecord;
import dk.dtu.tanktrouble.data.records.LevelRecord;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import static dk.dtu.tanktrouble.app.networking.Commands.*;

public record GameClient(Player player) implements Runnable {
	public static List<Player> playersInGame = new ArrayList<>();
	public static long timeOffset = 0;

	@SuppressWarnings("RedundantCast")
	@Override
	public void run() {
		try {
			Networking.getPlayerChannel().put(TO_PLAYER_HANDLER, PING, System.nanoTime());
			Object[] pingTest = player.channel.get(new ActualField(TO_GAME_CLIENT)
					, new FormalField(String.class)
					, new FormalField(Long.class)
					, new FormalField(Long.class));
			long currentTime = System.nanoTime();
			long oldTime = (long) pingTest[2];
			long serverTime = (long) pingTest[3];

			timeOffset = serverTime + (currentTime - oldTime) / 2 - currentTime;
			System.out.println("Time offset: " + timeOffset + ", My time: " + currentTime + ", Server time: " + serverTime + ", Ping: " + ((currentTime - oldTime) / 1e6));
		} catch (InterruptedException ignored) {
		}


		thread:
		while (true) {
			try {
				Object[] in = player.channel.get(new ActualField(TO_GAME_CLIENT)
						, new FormalField(String.class)
						, new FormalField(Object.class));
				String command = (String) in[1];
				ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream((byte[]) in[2]));
				Object[] args = (Object[]) inStream.readObject();

				switch (command) {
					case LOBBY_PLAYER_MAP -> {
						loadLevel((LevelRecord) args[0]);
						Main.onMapReceived.handle(null);
					}
					case PLAYERS_UPDATED -> {
						playersInGame = (ArrayList<Player>) args[0];
						OnlineController.chatSpace.put(LIST_OF_PLAYERS, playersInGame);
						if (Main.onPlayersUpdated != null)
							Main.onPlayersUpdated.handle(null);
					}
					case NUMBER_OF_ROUNDS_UPDATED -> onNewRoundSliderPositionReceived((int) args[0]);
					case NEW_SNAPSHOT -> TankTroubleAnimator.pendingSnapshots.put((GameSnapshot) args[0]);
					case SERVER_TO_PLAYER_CHAT_RESPONSE -> {
						if (OnlineController.onNewMessage != null) {
							OnlineController.chatSpace.put(CLIENT_TO_VISUALS_CHAT_UPDATE, (ChatRecord) args[0]);
							OnlineController.onNewMessage.handle(null);
						}
					}
					case SERVER_TO_CLIENT_QUIT_RECEIVED -> {
						player.channel.put(TO_PLAYER_HANDLER, CLIENT_TO_SERVER_QUIT_RECEIVED, new Object());
						Networking.reset();
						Main.toMainMenuEvent.handle(null);
						break thread;
					}
					case TIMEOUT_PING -> onTimeoutPingReceived();
					case POWER_UPS_UPDATED -> onPowerUpsUpdated((int[]) args[0]);
					case GAME_OVER_GOTO_END_SCREEN -> gameEnded();
					case RETURN_TO_LOBBY -> returnToLobby();
				}
			} catch (InterruptedException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	void returnToLobby() {
		Main.returnToLobby.handle(null);
	}

	void gameEnded() {
		//TODO reset stuff @alexander
		Main.onEndGame.handle(null);
	}

	void onPowerUpsUpdated(int[] in) {
		if (!player.isAdmin) {
			PowerUpData.updatePowerUp(in[0], in[1] == 1);
			LobbyController.updateListOfPowerUps.handle(null);
		}
	}

	void onTimeoutPingReceived() throws InterruptedException {
		player.channel.put(TO_TIMOUT, TIMEOUT_RESPONSE);
	}

	void onNewRoundSliderPositionReceived(int newPos) throws InterruptedException {
		if (!player.isAdmin) {
			LobbyController.chatSpace.put(NUMBER_OF_ROUNDS_UPDATED, newPos);
			if (LobbyController.onSliderUpdated != null)
				LobbyController.onSliderUpdated.handle(null);
		}
	}

	void loadLevel(LevelRecord record) throws InterruptedException {
		// Set the value of tankRecords
		TankTroubleAnimator.pendingSnapshots.getAll(new FormalField(GameSnapshot.class));
		TankTroubleAnimator.oldSnapshot = new GameSnapshot(record.tankRecords(), System.nanoTime() + GameClient.timeOffset);

		Wall.allWalls = record.map().getActiveWalls();
		GameMap.activeMap = record.map();
	}
}
