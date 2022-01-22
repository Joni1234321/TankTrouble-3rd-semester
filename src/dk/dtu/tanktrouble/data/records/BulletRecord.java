package dk.dtu.tanktrouble.data.records;

import dk.dtu.tanktrouble.app.model.Bullet;
import dk.dtu.tanktrouble.data.ColorSerialized;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record BulletRecord(String id, double x, double y, double size, double timeTillTimeout,
						   ColorSerialized colorRecord) implements Serializable, SpriteRecord {
	public BulletRecord(Bullet bullet, long now) {
		this(bullet.id, bullet.getX(), bullet.getY(), bullet.bulletSize, bullet.getTillTimeOut(now), new ColorSerialized(bullet.getColor()));
	}

	public static List<BulletRecord> generateRecords(List<Bullet> bullets, long now) {
		ArrayList<BulletRecord> result = new ArrayList<>();
		for (Bullet bullet : bullets)
			result.add(new BulletRecord(bullet, now));

		return result;
	}
}
