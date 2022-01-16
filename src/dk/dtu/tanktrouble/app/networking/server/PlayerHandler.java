package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
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
                    case KEY_EVENT -> playerPressedKey(cmd, (String) in[2]);

                }
            }
        } catch (InterruptedException e) {
            System.out.println("The player handler for: " + player + " has been shut down due to no response");
        }
    }

    void disconnectPlayer() {
        try {
            System.out.println("Trying to quit player");
            player.channel.put(TO_GAME_CLIENT, SERVER_TO_CLIENT_QUIT_RECEIVED, Commands.serialize());
            player.channel.get(new ActualField(TO_PLAYER_HANDLER), new ActualField(CLIENT_TO_SERVER_QUIT_RECEIVED), new FormalField(Object.class));


            System.out.println("Recived conformation");

            if (server.serverRequestSpace.queryp(new ActualField(CLOSE_LOBBY)) == null) {
                if (player.isAdmin) {
                    for (Player p : server.players) {
                        if (!p.equals(player)) {
                            p.isAdmin = true;
                            p.channel.put(TO_GAME_CLIENT, SERVER_TO_CLIENT_ADMIN_UPDATED, Commands.serialize());
                        }
                    }
                }
                server.players.remove(player);

                LobbyAuthorizer.onPlayerUpdated(server);
            }
            server.players.remove(player);
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
        byte[] data = Commands.serialize(player.getName(), player.getId(), message);
        server.broadcaster.sendToPlayers(SERVER_TO_PLAYER_CHAT_RESPONSE, data);
    }

    void playerPressedKey(String command, String event) {
        if (KEY_EVENT.equals(command))
            player.keyEvent(event.charAt(0) == '+', event.substring(1));
    }
}
