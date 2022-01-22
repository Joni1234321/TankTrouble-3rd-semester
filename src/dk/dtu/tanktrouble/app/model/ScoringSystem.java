package dk.dtu.tanktrouble.app.model;

import dk.dtu.tanktrouble.data.Player;

public class ScoringSystem {

	private static final int DEATH_PENALTY = 1, KILL_REWARD = 1, WIN_REWARD = 1;

	private static final int MIN_SCORE = 0;

	public static void penalizeKilled(Player killed) {
		if (killed.score > MIN_SCORE) {
			killed.score -= DEATH_PENALTY;
		}
	}

	public static void rewardKill(Player killer) {
		killer.score += KILL_REWARD;
	}

	public static void rewardWinner(Player winner) {
		winner.score += WIN_REWARD;
	}

}
