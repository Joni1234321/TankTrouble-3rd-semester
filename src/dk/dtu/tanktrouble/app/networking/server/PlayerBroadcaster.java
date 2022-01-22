package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.networking.Commands;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public record PlayerBroadcaster(GameServer server, SequentialSpace broadcastLog) implements Runnable {
	public PlayerBroadcaster(GameServer server) {
		this(server, new SequentialSpace());
	}

	@Override
	public void run() {
		try {
			while (true) {
				Object[] o = broadcastLog.get(new FormalField(String.class), new FormalField(Object.class));
				if (o[0].equals("STOP_DUPLICATOR")) break;

				// Broadcast
				for (Space channel : server.getAllPlayerChannels())
					channel.put(Commands.TO_GAME_CLIENT, o[0], o[1]);
			}
		} catch (InterruptedException ignored) {
		}
	}

	public void sendToPlayers(Object... fields) throws InterruptedException {
		broadcastLog.put(fields);
	}

	public void stopBroadcaster() {
		try {
			broadcastLog.put("STOP_DUPLICATOR", new Object());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
