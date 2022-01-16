package dk.dtu.tanktrouble.app.networking;

import dk.dtu.tanktrouble.app.networking.client.GameClient;
import dk.dtu.tanktrouble.app.networking.server.GameServer;
import dk.dtu.tanktrouble.data.Player;
import org.jspace.*;

import java.io.IOException;

public class Networking {
	// This is a class that exists to shorten Main.java
	// It has the functions of buttons

	private static Player player;

	// Since Player-object is created after join-acknowledgement the name of the player is not fully determined at this point
	private static String tentativePlayerName = "";
	private static double tentativeHue = Math.random() * 2 - 1;

	private static String address = "0.0.0.0";
	private static int port = 31145;


	public static GameServer startServer() {
		GameServer server = new GameServer(address, port);
		new Thread(server).start();
		return server;
	}

	public static GameClient startClient() {
		try {
			// Connect to spaces
			RemoteSpace lobby = getRemoteSpaceAt("lobby");

			// Generate player
			player = askLobbyForPlayer(lobby, tentativePlayerName, tentativeHue);
			if (player == null) return null;

			// Start new client and close lobby
			GameClient client = new GameClient(player);
			new Thread(client).start();
			lobby.close();

			return client;
		} catch (Exception ignored) {
			return null;
		}
	}

	private static Player askLobbyForPlayer (RemoteSpace lobby, String requestedName, double requestedHue) throws Exception {
		String joinID = Commands.generateRandomID();

		// Request player info
		lobby.put(Commands.LOBBY_PLAYER_JOIN_REQUEST, joinID, requestedName, requestedHue);
		Object[] in = lobby.get(new ActualField(Commands.LOBBY_NEW_ID),
				new ActualField(joinID),
				new FormalField(String.class),
				new FormalField(Boolean.class),
				new FormalField(Double.class));

		// On failure
		if(joinID.equals(Commands.SERVER_TO_PLAYER_ERROR_IN_CONNECTION)) {
			System.out.println("Could not connect");
			return null;
		}

		// Create new player
		String channelID = (String) in[2];
		boolean isAdmin = (boolean) in[3];
		double hue = (double) in[4];

		Player player = new Player(joinID, requestedName, hue, getRemoteSpaceAt(channelID), isAdmin);

		System.out.println(player);

		return player;
	}

	public static void reset() {
		try {
			((RemoteSpace)getCurrentPlayer().channel).close();
		} catch (Exception e) {
			System.out.println("Failed to close remote spaces");
		}
		player.channel = null;
		player = null;
		address = "0.0.0.0";
		port = 31145;
	}

	public static Player getCurrentPlayer() {
		return player;
	}
	public static void startGame() {
		if (player.isAdmin) {
			try {
				player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.PLAYER_REQUEST_SERVER_START, new Object());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void quitLobby() {
		if (player == null) return;
		try {
			player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.CLIENT_TO_SERVER_QUIT, new Object());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void sendChatMessage(String message) {
		if (message.equals("")) return;
		try {
			player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.PLAYER_SENDS_MESSAGE_TO_OTHER, message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static RemoteSpace getRemoteSpaceAt(String room) throws IOException {
		String uri = "tcp://" + address + ":" + port + "/" + room + "?keep";
		System.out.println("Connecting to " + uri);
		return new RemoteSpace(uri);
	}


	public static String getTentativePlayerName() {
		return tentativePlayerName;
	}
	public static void setTentativePlayerName(String tentativePlayerName) {
		Networking.tentativePlayerName = tentativePlayerName;
	}
	public static double getTentativeHue() {
		return tentativeHue;
	}
	public static void setTentativeHue(double tentativeHue) {
		Networking.tentativeHue = tentativeHue;
	}
	public static void setAddress(String address) {
		Networking.address = address;
	}
	public static String getAddress() {
		return Networking.address;
	}
	public static void setPort(int port) {
		Networking.port = port;
	}
	public static int getPort() {
		return Networking.port;
	}

}
