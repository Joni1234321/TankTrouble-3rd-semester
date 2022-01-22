package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.model.TankTrouble;
import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.records.LevelRecord;
import dk.dtu.tanktrouble.data.records.TankRecord;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jspace.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dk.dtu.tanktrouble.app.networking.Commands.*;

public class GameServer implements Runnable {
	private final int port;
	private final String address;

	// Call this when you want to stop the server
	public static EventHandler<Event> StopServer = event -> {
	};

	final TankTrouble tankTrouble;
	final PlayerBroadcaster broadcaster;
	private final Thread timeoutHandler;
	public int roundsLeft;
	SpaceRepository repository;
	Space lobby, serverRequestSpace;

	final List<Player> players = new ArrayList<>();

	public enum ProgramState {
		InLobby,
		InGame,
		ShuttingDown,
		GameEnded
	}

	public ProgramState state;

	public GameServer(String address, int port) {
		this.address = address;
		this.port = port;

		startServer();

		timeoutHandler = new Thread(new TimeoutHandler(this), "Timeout handler");
		timeoutHandler.start();

		// Start broadcaster
		broadcaster = new PlayerBroadcaster(this);
		new Thread(broadcaster, "Broadcaster Thread").start();

		// Start butler
		state = ProgramState.InLobby;
		new Thread(new LobbyAuthorizer(this, lobby, repository), "Lobby Authorizer Thread").start();

		// Start game
		tankTrouble = new TankTrouble(broadcaster);
	}

	public List<Space> getAllPlayerChannels() {
		List<Space> re = new ArrayList<>();
		players.forEach(x -> re.add(x.channel));
		return re;
	}

	public boolean playerWithIDExists(String id) {
		for (Player p : players)
			if (p.getId().equals(id)) return true;

		return false;
	}

	@Override
	public void run() {
		startGame();
	}

	private void startGame() {
		while (true) {
			try {
				// Wait until player has started the game
				serverRequestSpace.get(new ActualField(Commands.PLAYER_REQUEST_SERVER_START));

				// If the game is closed
				if (serverRequestSpace.queryp(new ActualField(CLOSE_LOBBY)) != null)
					return;

				// Start new round
				System.out.println("Starting game");
				state = ProgramState.InGame;

				for (Player player : players) player.resetKeys();

				while (state == ProgramState.InGame && roundsLeft != 0) {
					runNewMap();
				}
				if (state == ProgramState.ShuttingDown) {
					return;
				}
				state = ProgramState.InLobby;

				broadcaster.sendToPlayers(GAME_OVER_GOTO_END_SCREEN, serialize());
				LobbyAuthorizer.onPlayerUpdated(this);
				serverRequestSpace.get(new ActualField(Commands.PROCEED_FROM_END_SCREEN));


				if (serverRequestSpace.queryp(new ActualField(CLOSE_LOBBY)) != null)
					return;

				broadcaster.sendToPlayers(RETURN_TO_LOBBY, serialize());
				sendLobbySettingsToAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendLobbySettingsToAll() throws InterruptedException {
		players.forEach(p -> p.score = 0);
		LobbyAuthorizer.onPlayerUpdated(this);

		try {
			for (PowerUpData.PowerUpMetaData powerUp : PowerUpData.powerUps) {
				broadcaster.sendToPlayers(Commands.POWER_UPS_UPDATED
						, Commands.serialize(new int[]{powerUp.getId(), powerUp.isActive ? 1 : 0}));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//region start
	private void startServer() {
		lobby = new SequentialSpace();
		serverRequestSpace = new SequentialSpace();

		// Start repository
		repository = new SpaceRepository();

		repository.addGate(this.getUri());


		System.out.println("Hosting on: " + this.getUri());
		repository.add("lobby", lobby);

		// On Server Close
		StopServer = e -> Platform.runLater(this::onStopServer);
	}

	private void runNewMap() throws InterruptedException {
		// Remove old maps
		roundsLeft--;
		for (Space s : getAllPlayerChannels())
			s.getAll(new ActualField(Commands.LOBBY_PLAYER_MAP), new FormalField(Object.class));

		tankTrouble.generateNewLevel(players);

		LevelRecord levelInfo = new LevelRecord(tankTrouble.map, TankRecord.generateRecords(tankTrouble.tanks));
		broadcaster.sendToPlayers(Commands.LOBBY_PLAYER_MAP, Commands.serialize(levelInfo));
		broadcaster.sendToPlayers(Commands.PLAYERS_UPDATED, Commands.serialize(players));

		tankTrouble.startGameLoop();

	}
	//endregion

	public String generateUniquePlayerID() {
		String id;
		do {
			id = Commands.generateRandomID();
		} while (playerWithIDExists(id));
		return id;
	}

	//region events
	private void onStopServer() {
		timeoutHandler.interrupt();
		broadcaster.stopBroadcaster();
		tankTrouble.stopGameLoop();
		state = ProgramState.ShuttingDown;

		try {
			serverRequestSpace.put(Commands.CLOSE_LOBBY);
			lobby.put(Commands.LOBBY_PLAYER_JOIN_REQUEST, Commands.CLOSE_LOBBY, "", 0d);


			for (Player p : players) {
				p.channel.put(TO_PLAYER_HANDLER, Commands.CLIENT_TO_SERVER_QUIT, new Object());
			}

			serverRequestSpace.put(PLAYER_REQUEST_SERVER_START);
			serverRequestSpace.put(PROCEED_FROM_END_SCREEN);

			int count = 0;
			while (count < 3) {
				count++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				if (players.isEmpty()) break;
			}
			if (!players.isEmpty()) {
				System.out.println("Unable to close server, playersInGame remaining: \n" + Arrays.toString(players.toArray()));
				System.out.println("Forcing stop of player threads");
				for (Player p : players) {
					p.forceStopThread();
				}
			}
			repository.closeGates();
			repository.shutDown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	//endregion

	public String getUri() {
		return "tcp://" + address + ":" + port + "/?keep";
	}

	public String getGateUri(String id) {
		return "tcp://" + address + ":" + port + "/" + id + "?keep";
	}
}
