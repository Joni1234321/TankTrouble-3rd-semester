package dk.dtu.tanktrouble.app.controller.sprites.records;

import dk.dtu.tanktrouble.app.model.Tank;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public record TankRecord(String id, double x, double y, double angle, boolean isAlive) implements Serializable {
    public TankRecord (Tank tank) {
        this(tank.getId(), tank.getX(), tank.getY(), tank.getAngle(), tank.isAlive());
    }

    public static List<TankRecord> generateRecords (List<Tank> tanks) {
        ArrayList<TankRecord> result = new ArrayList<>();
        for (Tank tank : tanks)
            result.add(new TankRecord (tank));

        return result;
    }
}
