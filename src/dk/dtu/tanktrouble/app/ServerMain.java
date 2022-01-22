package dk.dtu.tanktrouble.app;


import dk.dtu.tanktrouble.app.networking.server.GameServer;

import java.util.Scanner;

public class ServerMain {
	// RUN THIS IF WANT TO RUN STANDALONE SERVER
	public static void main(String[] args) {
		GameServer server = new GameServer("0.0.0.0", 31145);

		new Thread(server, "GameServer Thread").start();


		Scanner in = new Scanner(System.in);

		while (true) {
			if (in.nextLine().equals("quit")) {
				GameServer.StopServer.handle(null);
				System.out.println("Quitting server");
				break;
			}
		}

	}
}
