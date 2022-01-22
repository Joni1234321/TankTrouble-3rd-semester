package dk.dtu.tanktrouble.data.records;

import dk.dtu.tanktrouble.app.model.Tank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record TankRecord(String id, double x, double y, double angle, boolean isAlive,
						 double hue) implements Serializable, SpriteRecord {
	public TankRecord(Tank tank) {
		this(tank.getId(), tank.getX(), tank.getY(), tank.getAngle(), tank.isAlive(), tank.player.hue);
	}

	public static List<TankRecord> generateRecords(List<Tank> tanks) {
		ArrayList<TankRecord> result = new ArrayList<>();
		for (Tank tank : tanks)
			result.add(new TankRecord(tank));

		return result;
	}
}
