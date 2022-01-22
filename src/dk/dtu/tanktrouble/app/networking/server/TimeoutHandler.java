package dk.dtu.tanktrouble.app.networking.server;

import dk.dtu.tanktrouble.app.networking.Commands;
import dk.dtu.tanktrouble.data.Player;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.LinkedList;
import java.util.List;

import static dk.dtu.tanktrouble.app.networking.Commands.*;

public record TimeoutHandler(GameServer server) implements Runnable {
	@Override
	public void run() {
		while (true) {
			List<Player> needsToBeChecked = new LinkedList<>();
			try {
				for (Player p : server.players) {
					Space c = p.channel;

					if (server.state == GameServer.ProgramState.InGame) {
						List<Object[]> unread = c.queryAll(new ActualField(TO_GAME_CLIENT)
								, new FormalField(String.class)
								, new FormalField(Object.class));

						if (unread != null && unread.size() > 20) {
							c.put(TO_GAME_CLIENT, TIMEOUT_PING, serialize());
							needsToBeChecked.add(p);
						}
					} else {
						c.put(TO_GAME_CLIENT, TIMEOUT_PING, serialize());
						needsToBeChecked.add(p);
					}
				}
				Thread.sleep(10000);
				for (Player p : needsToBeChecked) {
					if (p.channel.getp(new ActualField(TO_TIMOUT), new ActualField(TIMEOUT_RESPONSE)) == null) {
						System.out.println(p + " has been kicked due to timeout");
						p.channel.put(TO_PLAYER_HANDLER, CLOSE_DUE_TO_TIMOUT, Commands.serialize());
					}
				}
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
