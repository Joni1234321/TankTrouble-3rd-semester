package dk.dtu.tanktrouble.app.controller.sprites.records;

import dk.dtu.tanktrouble.app.model.Bullet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record BulletRecord(int id, double x, double y, double timeTillTimeout) implements Serializable {
    public BulletRecord(Bullet bullet, long now) {
        this(bullet.getId(), bullet.getX(), bullet.getY(), bullet.getTillTimeOut(now));
    }

    public static List<BulletRecord> generateRecords (List<Bullet> bullets, long now) {
        ArrayList<BulletRecord> result = new ArrayList<>();
        for (Bullet bullet : bullets)
            result.add(new BulletRecord (bullet, now));

        return result;
    }
}
