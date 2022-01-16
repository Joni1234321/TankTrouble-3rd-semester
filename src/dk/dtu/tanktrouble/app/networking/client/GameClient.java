package dk.dtu.tanktrouble.app.networking.client;

import dk.dtu.tanktrouble.app.Main;
import dk.dtu.tanktrouble.app.controller.TankTroubleAnimator;
import dk.dtu.tanktrouble.app.controller.sprites.records.BulletRecord;
import dk.dtu.tanktrouble.app.controller.sprites.records.LevelRecord;
import dk.dtu.tanktrouble.app.controller.sprites.records.TankRecord;
import dk.dtu.tanktrouble.app.controller.LobbyController;
import dk.dtu.tanktrouble.app.model.gameMap.GameMap;
import dk.dtu.tanktrouble.app.model.gameMap.Wall;
import dk.dtu.tanktrouble.app.networking.Networking;
import dk.dtu.tanktrouble.data.GameSnapshot;
import dk.dtu.tanktrouble.data.Player;
import org.jspace.ActualField;
import org.jspace.FormalField;

import static dk.dtu.tanktrouble.app.networking.Commands.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class GameClient implements Runnable {
    // This file manages all the input that are directed towards the player and distributes it to the controllers
    final Player player;
    public static List<Player> playersInGame = new ArrayList<>();


    public GameClient(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
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
                        LobbyController.chatSpace.put(LIST_OF_PLAYERS, args[0]);
                        if (Main.onPlayersUpdated != null)
                            Main.onPlayersUpdated.handle(null);
                        playersInGame = (ArrayList<Player>) args[0];
                    }
                    case NEW_SNAPSHOT -> {
                        long time = (long) args[0];
                        List<TankRecord> tankRecords = (ArrayList<TankRecord>) args[1];
                        List<BulletRecord> bulletRecords = (ArrayList<BulletRecord>) args[2];
                        TankTroubleAnimator.pendingSnapshots.put(new GameSnapshot(tankRecords, bulletRecords, time));
                    }
                    case SERVER_TO_PLAYER_CHAT_RESPONSE -> {
                        LobbyController.chatSpace.put(CLIENT_TO_VISUALS, args[0], args[1], args[2]);
                        LobbyController.onNewMessage.handle(null);
                    }
                    case SERVER_TO_CLIENT_QUIT_RECEIVED -> {
                        //TODO save exit rest of the client
                        player.channel.put(TO_PLAYER_HANDLER,CLIENT_TO_SERVER_QUIT_RECEIVED, new Object());
                        Networking.reset();
                        Main.toMainMenuEvent.handle(null);
                        break thread;
                    }
                    case SERVER_TO_CLIENT_ADMIN_UPDATED -> {
                        player.isAdmin = true;
                        //TODO if in lobby, update visuals
                    }
                }
            } catch (InterruptedException | ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    void loadLevel(LevelRecord record) {
        // Set the value of tankRecords
        TankTroubleAnimator.oldSnapshot = new GameSnapshot(record.tankRecords(), new ArrayList<>(), System.nanoTime());

        Wall.allWalls = record.map().getActiveWalls();
        GameMap.activeMap = record.map();

        System.out.println("Map from server shown");
    }

    public static Player getPlayerById (String id) {
        for (Player player : playersInGame) {
            if (player.getId().equals(id)) return player;
        }
        return null;
    }
}
