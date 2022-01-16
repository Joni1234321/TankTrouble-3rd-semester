package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
import org.jspace.*;

import java.util.Arrays;

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

                switch (server.state) {
                    case InLobby -> playerJoined(joinID, server.generateUniquePlayerID(), (String) request[2], (double) request[3]);
                    case InGame -> playerJoinedButDenied(joinID);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void playerJoined(String joinID, String playerID, String name, double hue) throws Exception {
        Player player = new Player(playerID, name, hue, new SequentialSpace(), server.players.isEmpty());

        // Add player to server
        repository.add(playerID, player.channel);
        server.players.add(player);

        onPlayerUpdated(server);

        Thread playerThread = new Thread(new PlayerHandler(server, player));
        player.setThread(playerThread);
        playerThread.start();

        // Send back the player info
        lobby.put(Commands.LOBBY_NEW_ID, joinID, playerID, player.isAdmin, hue);
    }

    private void playerJoinedButDenied(String joinID) throws Exception {
        lobby.put(Commands.LOBBY_NEW_ID, joinID, Commands.SERVER_TO_PLAYER_ERROR_IN_CONNECTION, false);
    }

    public static void onPlayerUpdated(GameServer server) throws InterruptedException {
        // Called every time a player is added with the parameter of all playersInGame added
        server.broadcaster.sendToPlayers(Commands.PLAYERS_UPDATED, Commands.serialize(server.players));
        System.out.println("Current playersInGame: " + Arrays.toString(server.players.toArray()));
    }


}
