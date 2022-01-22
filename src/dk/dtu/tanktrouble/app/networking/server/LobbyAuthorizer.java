package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.records.ChatRecord;
import org.jspace.*;

import java.util.Arrays;

import static dk.dtu.tanktrouble.app.networking.Commands.SERVER_TO_PLAYER_CHAT_RESPONSE;
import static dk.dtu.tanktrouble.app.networking.Commands.serialize;

public record LobbyAuthorizer(GameServer server, Space lobby, SpaceRepository repository) implements Runnable {
	// This handle new player request
	@Override
	public void run() {
		while (true) {
			try {
				Object[] request = lobby.get(new ActualField(Commands.LOBBY_PLAYER_JOIN_REQUEST), new FormalField(String.class), new FormalField(String.class), new FormalField(Double.class));
				String joinID = (String) request[1];

				if (joinID.equals(Commands.CLOSE_LOBBY))
					break;

				if (server.state == GameServer.ProgramState.InLobby) {
					playerJoined(joinID, server.generateUniquePlayerID(), (String) request[2], (double) request[3]);
				} else {
					playerJoinedButDenied(joinID);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void playerJoined(String handshake, String playerID, String name, double hue) throws Exception {
		if (name.equals("")) {
			name = "Player" + (server.players.size() + 1);
		}

		Player player = new Player(playerID, name, hue, new SequentialSpace(), server.players.isEmpty());

		// Add player to server
		repository.add(playerID, player.channel);
		server.players.add(player);

		onPlayerUpdated(server);

		Thread playerThread = new Thread(new PlayerHandler(server, player), "PlayerHandler " + player.getId() + " Thread");
		player.setThread(playerThread);
		playerThread.start();

		// Send back the player info
		lobby.put(Commands.LOBBY_NEW_ID, handshake, playerID, player.isAdmin, name, hue);

		server.broadcaster.sendToPlayers(SERVER_TO_PLAYER_CHAT_RESPONSE, serialize(new ChatRecord("", name + " has joined the game")));

		if (!player.isAdmin)
			sendLobbySettings(player.channel);
	}

	private void sendLobbySettings(Space channel) {
		int sliderPosition = server.roundsLeft == (int) 1e9 ? 19 : (server.roundsLeft <= 10 ? server.roundsLeft : (10 + (server.roundsLeft - 10) / 5));

		try {
			channel.put(Commands.TO_GAME_CLIENT, Commands.NUMBER_OF_ROUNDS_UPDATED, Commands.serialize(sliderPosition));
			for (PowerUpData.PowerUpMetaData powerUp : PowerUpData.powerUps) {
				channel.put(Commands.TO_GAME_CLIENT
						, Commands.POWER_UPS_UPDATED
						, Commands.serialize(new int[]{powerUp.getId(), powerUp.isActive ? 1 : 0}));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void playerJoinedButDenied(String handshake) throws Exception {
		lobby.put(Commands.LOBBY_NEW_ID, handshake, Commands.SERVER_TO_PLAYER_ERROR_IN_CONNECTION, false, "", 0.0);
	}

	public static void onPlayerUpdated(GameServer server) throws InterruptedException {
		// Called every time a player is added with the parameter of all playersInGame added
		server.broadcaster.sendToPlayers(Commands.PLAYERS_UPDATED, Commands.serialize(server.players));
		System.out.println("Current playersInGame: " + Arrays.toString(server.players.toArray()));
	}


}
