package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.records.ChatRecord;
import org.jspace.ActualField;
import org.jspace.FormalField;

import static dk.dtu.tanktrouble.app.networking.Commands.*;

public record PlayerHandler(GameServer server, Player player) implements Runnable {
	@Override
	public void run() {
		Object[] in;
		String cmd;

		try {
			thread:
			while (true) {
				in = player.channel.get(new ActualField(TO_PLAYER_HANDLER), new FormalField(String.class), new FormalField(Object.class));
				cmd = (String) in[1];

				switch (cmd) {
					case PLAYER_REQUEST_SERVER_START -> playerRequestedServerStart();
					case PLAYER_SENDS_MESSAGE_TO_OTHER -> playerSendMessageToOther((String) in[2]);
					case CLOSE_SERVER -> closeServer();
					case CLIENT_TO_SERVER_QUIT -> {
						disconnectPlayer();
						break thread;
					}
					case PING -> getPing((long) in[2]);
					case NUMBER_OF_ROUNDS_UPDATED -> updateNumberOfRounds((int[]) in[2]);
					case KEY_EVENT -> playerPressedKey(cmd, (String) in[2]);
					case CLOSE_DUE_TO_TIMOUT -> {
						disconnectWithoutConformation();
						break thread;
					}
					case RETURN_TO_LOBBY -> returnToLobby();
					case POWER_UPS_UPDATED -> updateActivePowerUps((int[]) in[2]);
				}
			}
		} catch (InterruptedException e) {
			System.out.println("The player handler for: " + player + " has been shut down due to no response");
		}
	}

	void returnToLobby() throws InterruptedException {
		if (player.isAdmin) {
			server.serverRequestSpace.put(PROCEED_FROM_END_SCREEN);
		}
	}

	void updateActivePowerUps(int[] in) throws InterruptedException {
		if (player.isAdmin) {
			PowerUpData.updatePowerUp(in[0], in[1] == 1);
			server.broadcaster.sendToPlayers(POWER_UPS_UPDATED, serialize(in));
		}
	}

	void updateNumberOfRounds(int[] in) throws InterruptedException {
		if (player.isAdmin) {
			//TODO gør noget med rounds
			server.roundsLeft = in[1];
			int sliderPosition = in[0];
			server.broadcaster.sendToPlayers(NUMBER_OF_ROUNDS_UPDATED, serialize(sliderPosition));
		}
	}

	void disconnectPlayer() {
		try {
			System.out.println("Trying to quit player");
			player.channel.put(TO_GAME_CLIENT, SERVER_TO_CLIENT_QUIT_RECEIVED, Commands.serialize());
			player.channel.get(new ActualField(TO_PLAYER_HANDLER), new ActualField(CLIENT_TO_SERVER_QUIT_RECEIVED), new FormalField(Object.class));
			System.out.println("Received confirmation");

			removePlayer();
		} catch (InterruptedException ignored) {
		}
	}

	void disconnectWithoutConformation() {
		try {
			player.channel.put(TO_GAME_CLIENT, SERVER_TO_CLIENT_QUIT_RECEIVED, Commands.serialize());
			removePlayer();
		} catch (InterruptedException ignored) {
		}
	}

	public void removePlayer() throws InterruptedException {
		if (server.serverRequestSpace.queryp(new ActualField(CLOSE_LOBBY)) == null) {
			server.broadcaster.sendToPlayers(SERVER_TO_PLAYER_CHAT_RESPONSE, serialize(new ChatRecord("", player.getName() + " has left the game")));
			server.players.remove(player);
			LobbyAuthorizer.onPlayerUpdated(server);
		}
		server.players.remove(player);
		server.repository.closeGate(server.getGateUri(player.getId()));
	}

	void getPing(long oriTime) {
		try {
			player.channel.put(TO_GAME_CLIENT, PING, oriTime, System.nanoTime());
		} catch (InterruptedException ignored) {
		}
	}

	void playerRequestedServerStart() throws InterruptedException {
		if (player.isAdmin)
			server.serverRequestSpace.put(PLAYER_REQUEST_SERVER_START);
	}

	void closeServer() {
		if (player.isAdmin) GameServer.StopServer.handle(null);
	}

	void playerSendMessageToOther(String message) throws InterruptedException {
		byte[] data = Commands.serialize(new ChatRecord(player.getName(), message));
		server.broadcaster.sendToPlayers(SERVER_TO_PLAYER_CHAT_RESPONSE, data);
	}

	void playerPressedKey(String command, String event) {
		if (KEY_EVENT.equals(command))
			player.keyEvent(event.charAt(0) == '+', event.substring(1));
	}
}
