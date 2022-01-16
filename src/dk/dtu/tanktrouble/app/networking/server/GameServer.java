package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.controller.sprites.records.LevelRecord;
import dk.dtu.tanktrouble.app.controller.sprites.records.TankRecord;
import dk.dtu.tanktrouble.app.model.TankTrouble;
import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
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
    public static EventHandler<Event> StopServer = event -> {};

    final TankTrouble tankTrouble;
    final PlayerBroadcaster broadcaster;
    SpaceRepository repository;
	Space lobby, serverRequestSpace;

	final List<Player> players = new ArrayList<>();

    public enum ProgramState {
        InLobby,
        InGame,
        ShuttingDown
    }
    public ProgramState state;

    public GameServer(String address, int port) {
		this.address = address;
		this.port = port;

        startServer();

        // Start broadcaster
        broadcaster = new PlayerBroadcaster(this);
        new Thread(broadcaster).start();

        // Start butler
        state = ProgramState.InLobby;
        new Thread(new LobbyAuthorizer(this, lobby, repository)).start();

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
        try {
            // Wait until player has started the game
            serverRequestSpace.get(new ActualField(Commands.PLAYER_REQUEST_SERVER_START));

            // If the game is closed
            if(serverRequestSpace.queryp(new ActualField(CLOSE_LOBBY)) != null)
                return;

            // Start new round
            System.out.println("Starting game");
            state = ProgramState.InGame;

            while (state == ProgramState.InGame) runNewMap();

        } catch (Exception e) { e.printStackTrace(); }
    }


    //region start
    private void startServer () {
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
        for (Space s : getAllPlayerChannels())
            s.getAll(new ActualField(Commands.LOBBY_PLAYER_MAP), new FormalField(Object.class));

        tankTrouble.generateNewLevel(players);

        LevelRecord levelInfo = new LevelRecord(tankTrouble.map, TankRecord.generateRecords(tankTrouble.tanks));
        broadcaster.sendToPlayers(Commands.LOBBY_PLAYER_MAP, Commands.serialize(levelInfo));


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
    private void onStopServer ()  {
        broadcaster.stopBroadcaster();
        tankTrouble.stopGameLoop();
        state = ProgramState.ShuttingDown;

        try {
            serverRequestSpace.put(Commands.CLOSE_LOBBY);
            lobby.put(Commands.LOBBY_PLAYER_JOIN_REQUEST, Commands.CLOSE_LOBBY, "", 0d);


            for(Player p: players){
                p.channel.put(TO_PLAYER_HANDLER, Commands.CLIENT_TO_SERVER_QUIT, new Object());
            }

            serverRequestSpace.put(PLAYER_REQUEST_SERVER_START);

            int count = 0;
            while (count < 10){
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                if(players.isEmpty()) break;
            }
            if(players.isEmpty()) {
                repository.closeGates();
                repository.shutDown();
                System.out.println("Server shut down complete");
            } else {
                System.out.println("Unable to close server, playersInGame remaining: \n" + Arrays.toString(players.toArray()));
                System.out.println("Forcing stop of player threads");
                for(Player p: players){
                    p.forceStopThread();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //endregion

    public String getUri() {
        return "tcp://" + address + ":" + port + "/?keep";
    }
}
