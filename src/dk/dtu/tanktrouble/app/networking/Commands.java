package dk.dtu.tanktrouble.app.networking;


import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Commands {
	// FROM_TO or WHERE
	public static final String
			PLAYER_REQUEST_SERVER_START = "start",
			CLIENT_TO_SERVER_QUIT = "quit",
			PLAYER_SENDS_MESSAGE_TO_OTHER = "chat",
			LOBBY_PLAYER_JOIN_REQUEST = "Join",
			LOBBY_NEW_ID = "idReturn",
			LOBBY_PLAYER_MAP = "map",
			NEW_SNAPSHOT = "snapshot",
			LIST_OF_PLAYERS = "currentPlayers",
			SERVER_TO_PLAYER_ERROR_IN_CONNECTION = "errorCouldNotConnect",
			SERVER_TO_PLAYER_CHAT_RESPONSE = "chatResponse",
			CLIENT_TO_VISUALS_CHAT_UPDATE = "latestChatResponse",
			KEY_EVENT = "keyEvent",
			CLIENT_TO_SERVER_QUIT_RECEIVED = "QUIT_RECEIVED_CLIENT",
			SERVER_TO_CLIENT_QUIT_RECEIVED = "QUIT_RECEIVED_SERVER",
			SCOREBOARD_UPDATED = "SCOREBOARD_UPDATED",
			CLOSE_SERVER = "closeServer",
			CLOSE_LOBBY = "close lobby",
			NUMBER_OF_ROUNDS_UPDATED = "updateNumberOfRounds",
			TIMEOUT_PING = "serverToClientTimeoutPing",
			TIMEOUT_RESPONSE = "clientToServerTimeoutResponse",
			CLOSE_DUE_TO_TIMOUT = "closeDueToTimeout",
			PING = "ping",
			POWER_UPS_UPDATED = "powerUpsUpdated",
			GAME_OVER_GOTO_END_SCREEN = "gameOverGoToEndScreen",
			PROCEED_FROM_END_SCREEN = "proceedFromEndScreen",
			RETURN_TO_LOBBY = "goBackToLobby";

	// Simple
	public static final String
			PLAYERS_UPDATED = "playersUpdated",
			TO_GAME_CLIENT = "toClient",
			TO_PLAYER_HANDLER = "toServer",
			TO_TIMOUT = "toTimout";

	public static byte[] serialize(Object... fields) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream oStream = new ObjectOutputStream(byteStream);
			oStream.writeObject(Arrays.copyOf(fields, fields.length));

			return byteStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String generateRandomID() {
		byte[] array = new byte[8];
		for (int i = 0; i < 8; i++)
			array[i] = (byte) (97 + (Math.random() * 24));

		return new String(array, StandardCharsets.US_ASCII);
	}

}
