package dk.dtu.tanktrouble.app.networking;

import dk.dtu.tanktrouble.app.networking.client.GameClient;
import dk.dtu.tanktrouble.app.networking.server.GameServer;
import dk.dtu.tanktrouble.data.Player;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.io.IOException;

public class Networking {
	// This is a class that exists to shorten Main.java
	// It has the functions of buttons

	private static Player player;

	// Since Player-object is created after join-acknowledgement the name of the player is not fully determined at this point
	private static String tentativePlayerName = "";
	private static double tentativeHue = Math.random() * 2 - 1;

	public final static String standardAddress = "0.0.0.0";
	public final static int standardPort = 31145;
	private static String address = standardAddress;
	private static int port = standardPort;

	public static Space getPlayerChannel() {
		return player.channel;
	}

	public static void startServer() {
		new Thread(new GameServer(address, port), "GameServer Thread").start();
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
			new Thread(client, "Game Client " + player.getId() + " Thread").start();
			lobby.close();

			return client;
		} catch (Exception ignored) {
			return null;
		}
	}

	private static Player askLobbyForPlayer(RemoteSpace lobby, String requestedName, double requestedHue) throws Exception {
		String handshake = Commands.generateRandomID();

		// Request player info
		lobby.put(Commands.LOBBY_PLAYER_JOIN_REQUEST, handshake, requestedName, requestedHue);
		Object[] in = lobby.get(new ActualField(Commands.LOBBY_NEW_ID),
				new ActualField(handshake),
				new FormalField(String.class),
				new FormalField(Boolean.class),
				new FormalField(String.class),
				new FormalField(Double.class));

		// Create new player
		String id = (String) in[2];
		boolean isAdmin = (boolean) in[3];
		String name = (String) in[4];
		double hue = (double) in[5];

		// On failure
		if (id.equals(Commands.SERVER_TO_PLAYER_ERROR_IN_CONNECTION)) {
			System.out.println("Could not connect");
			return null;
		}

		Player player = new Player(id, name, hue, getRemoteSpaceAt(id), isAdmin);

		System.out.println(player);

		return player;
	}

	public static void reset() {
		try {
			((RemoteSpace) player.channel).close();
		} catch (Exception e) {
			System.out.println("Failed to close remote spaces");
		}
		player.channel = null;
		player = null;
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
			if (player.isAdmin) {
				player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.CLOSE_SERVER, new Object());
			} else {
				player.channel.put(Commands.TO_PLAYER_HANDLER, Commands.CLIENT_TO_SERVER_QUIT, new Object());
			}
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
