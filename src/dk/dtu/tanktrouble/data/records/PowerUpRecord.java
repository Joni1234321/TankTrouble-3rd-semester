package dk.dtu.tanktrouble.data.records;

import dk.dtu.tanktrouble.app.model.powerups.PowerUp;
import dk.dtu.tanktrouble.app.model.powerups.TankMod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record PowerUpRecord(String id, double x, double y, double angle,
							Class<? extends TankMod> tankMod) implements Serializable, SpriteRecord {

	public PowerUpRecord(PowerUp powerUp) {
		this(powerUp.id, powerUp.x, powerUp.y, powerUp.angle, powerUp.tankMod.getClass());
	}

	public static List<PowerUpRecord> generateRecords(List<PowerUp> powerUps) {
		ArrayList<PowerUpRecord> result = new ArrayList<>();
		for (PowerUp powerUp : powerUps)
			result.add(new PowerUpRecord(powerUp));

		return result;
	}
}
